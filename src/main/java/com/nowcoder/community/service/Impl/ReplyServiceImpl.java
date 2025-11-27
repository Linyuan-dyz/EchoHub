package com.nowcoder.community.service.Impl;

import com.nowcoder.community.common.kafka.ReplyPublisher;
import com.nowcoder.community.domain.dto.ReplyDTO;
import com.nowcoder.community.domain.po.ReplyContent;
import com.nowcoder.community.domain.po.ReplySection;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.domain.vo.ReplyContentVO;
import com.nowcoder.community.mapper.ReplyContentMapper;
import com.nowcoder.community.mapper.ReplyIndexMapper;
import com.nowcoder.community.mapper.ReplySectionMapper;
import com.nowcoder.community.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;

@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {

    private final ReplySectionMapper replySectionMapper;
    private final ReplyIndexMapper replyIndexMapper;
    private final ReplyContentMapper replyContentMapper;
    private final StringRedisTemplate  stringRedisTemplate;
    private final RedisTemplate<String, ReplySection> sectionRedisTemplate;
    private final RedisTemplate<String, ReplyContentVO> contentRedisTemplate;
    private final ReplyPublisher replyPublisher;

    /**
     * 获取评论区基础信息
     * @param discussPostId
     * @return
     */
    @Override
    public Result getReplySectionDetails(int discussPostId) {
        String replySectionCacheKey = REPLY_SEC_CACHE_KEY + discussPostId;
        ReplySection replySection = sectionRedisTemplate.opsForValue().get(replySectionCacheKey);
        if (replySection == null) {
            //  读请求重建缓存，依靠消息队列完成，防止多次重复重建缓存
            replyPublisher.publishReconstructReplySection(discussPostId);
        }
        String listReplyCacheKey = LIST_REPLY_KEY + discussPostId;
        Set<String> replyUserList = stringRedisTemplate.opsForZSet().range(listReplyCacheKey, 0, -1);
        if (replyUserList == null || replyUserList.isEmpty()) {
            //  预加载评论关系缓存和评论内容
            replyPublisher.publishReconstructReplyIndex(discussPostId);
        }
        //  TODO: 等待缓存重建完成，暂时使用轮询的方式，后续可以改进为异步通知的方式
        //  得到反序列化后的结果
        replySection = sectionRedisTemplate.opsForValue().get(replySectionCacheKey);
        return Result.ok(replySection);
    }

    /**
     * 获取评论的具体内容
     * @param rpid
     * @return
     */
    @Override
    public Result getReplyDetails(int rpid) {
        String rootReplyCacheKey = ROOT_REPLY_KEY + rpid;
        Set<String> rootReplyCache = stringRedisTemplate.opsForZSet().range(rootReplyCacheKey, 0, -1);
        if (rootReplyCache == null || rootReplyCache.isEmpty()) {
            //  读请求重建缓存，依靠消息队列完成，防止多次重复重建缓存
            replyPublisher.publishReconstructReplyContent(rpid);
        }
        rootReplyCache = stringRedisTemplate.opsForZSet().range(rootReplyCacheKey, 0, -1);
        List<ReplyContentVO> contentList = getReplyContentFromCache(rootReplyCache);
        return Result.ok(contentList);
    }

    /**
     * 获取根评论数据列表
     * @param discussPostId
     * @return
     */
    @Override
    public Result getReplyList(int discussPostId) {
        String listReplyCacheKey = LIST_REPLY_KEY + discussPostId;
        Set<String> replyIndexList = stringRedisTemplate.opsForZSet().range(listReplyCacheKey, 0, -1);
        if (replyIndexList == null || replyIndexList.isEmpty()) {
            //  预加载评论关系缓存和评论内容
            replyPublisher.publishReconstructReplyIndex(discussPostId);
        }
        replyIndexList = stringRedisTemplate.opsForZSet().range(listReplyCacheKey, 0, -1);
        List<ReplyContentVO> contentList = getReplyContentFromCache(replyIndexList);
        return Result.ok(contentList);
    }

    @Override
    public Result addReply(int discussPostId, ReplyDTO replyDTO) {
        return null;
    }

    @Override
    public Result deleteReply(int rpid) {
        return null;
    }

    /**
     * 发送消息给消息队列后等待缓存重建，使用有界等待+线程阻塞的方式等待重建完成
     * 如果不存在数据或重建超时，则返回空对象，同时也可以防止缓存穿透问题
     * @param cacheKey
     * @param redisTemplate
     * @return
     * @param <T>
     */
    private <T> T waitForCacheRebuild(String cacheKey, RedisTemplate<String, T> redisTemplate, Class<T> clazz) {
        // 3) 有界等待 + 退避（避免忙等）
        long deadline = System.nanoTime() + MAX_WAIT_MS * 1_000_000L;
        for (int i = 0;; i++) {
            //  进行一次缓存读取尝试
            T firstTry = redisTemplate.opsForValue().get(cacheKey);
            if (firstTry != null) {
                //  如果成功获取缓存数据，则直接返回
                return firstTry;
            }
            //  如果缓存获取超时，则返回降级处理后的对象
            if (System.nanoTime() >= deadline) break;

            long backoff = BACKOFF_MS[Math.min(i, BACKOFF_MS.length - 1)];
            // 0~5ms 抖动
            backoff += ThreadLocalRandom.current().nextLong(0, 5);
            try {
                Thread.sleep(backoff);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        //  重建超时处理，返回降级对象，防止缓存穿透
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            if (!ctor.canAccess(null)) ctor.setAccessible(true);
            T wrongInstance = (T) ctor.newInstance();
            Field f = wrongInstance.getClass().getDeclaredField("id");
            if (!f.canAccess(wrongInstance)) f.setAccessible(true);
            f.set(wrongInstance, -1);
            return wrongInstance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final long REBUILD_PX_MS   = 3_000;   // 重建标记 TTL
    private static final long MAX_WAIT_MS     = 60;      // 读侧最多等待60ms
    private static final long[] BACKOFF_MS    = new long[]{5, 10, 20, 15}; // 带抖动

    private List<ReplyContentVO> getReplyContentFromCache(Set<String> replyIndexList) {
        List<ReplyContentVO> contentList = new ArrayList<>();
        if (replyIndexList != null) {
            contentList = contentRedisTemplate.opsForValue().multiGet(
                    replyIndexList.stream().map(id -> CONTENT_REPLY_KEY + id).toList());
        }
        return contentList;
    }

    //  评论区缓存key，形式为section:reply:pattern:{discussPostId}，value值为JSON序列化的字符串，储存评论区基础信息
    public static final String REPLY_SEC_CACHE_KEY = "section:reply:pattern:";
    //  根评论列表缓存key，形式为list:reply:pattern:{discussPostId},value值为zset的集合,member为reid，score可以是时间戳和点赞数
    public static final String LIST_REPLY_KEY = "list:reply:pattern:";
    //  评论内容key，形式为content:reply:pattern:{rpid}，value值为JSON序列化的字符串，储存评论内容和评论索引数据
    public static final String CONTENT_REPLY_KEY = "content:reply:pattern:";
    //  根评论关系key，形式为root:reply:pattern:{rpid},value值为zset的集合，member为子评论id，score可以是时间戳和点赞数
    public static final String ROOT_REPLY_KEY = "root:reply:pattern:";
}
