package com.nowcoder.community.common.kafka;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nowcoder.community.domain.entity.EventNotice;
import com.nowcoder.community.domain.entity.LikeNotice;
import com.nowcoder.community.domain.entity.NoticePayload;
import com.nowcoder.community.domain.entity.ReplyNotice;
import com.nowcoder.community.domain.po.ReplyContent;
import com.nowcoder.community.domain.po.ReplyIndex;
import com.nowcoder.community.domain.po.ReplySection;
import com.nowcoder.community.domain.vo.ReplyContentVO;
import com.nowcoder.community.mapper.ReplyContentMapper;
import com.nowcoder.community.mapper.ReplyDetailsMapper;
import com.nowcoder.community.mapper.ReplyIndexMapper;
import com.nowcoder.community.mapper.ReplySectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReplyListener {

    private static final String REPLY_SECTION_TOPIC = "reply.section.notice";
    private static final String REPLY_INDEX_TOPIC = "reply.index.notice";
    private static final String REPLY_DETAILS_TOPIC = "reply.details.notice";
    private final RedisTemplate<String, ReplySection> sectionRedisTemplate;    //redis序列化工具，实现对象的存取
    private final RedisTemplate<String, ReplyContentVO> contentRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ReplySectionMapper replySectionMapper;
    private final ReplyIndexMapper replyIndexMapper;
    private final ReplyDetailsMapper replyDetailsMapper;


    /**
     * 缓存帖子下的评论区基础信息
     * @param eventNotice
     * @param topic
     * @param offset
     */
    @KafkaListener(topics = REPLY_SECTION_TOPIC)
    public void onSectionMessage(@Payload EventNotice eventNotice,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.OFFSET) long offset) {
        NoticePayload payload = eventNotice.getExtra();
        if (payload instanceof ReplyNotice replySectionNotice) {
            int discussPostId = replySectionNotice.getDiscussPostId();
            String replySectionKey = REPLY_SEC_CACHE_KEY + discussPostId;
            ReplySection replySection = sectionRedisTemplate.opsForValue().get(replySectionKey);
            if (replySection == null) {
                //  如果缓存不存在，则执行重建缓存操作
                QueryWrapper<ReplySection> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("post_id", discussPostId);
                replySection = replySectionMapper.selectOne(queryWrapper);
                if (replySection != null) {
                    sectionRedisTemplate.opsForValue().set(replySectionKey, replySection);
                } else {
                    //  缓存错误对象来防缓存穿透
                    sectionRedisTemplate.opsForValue().set(replySectionKey, ReplySection.builder().id(-1).build());
                }

            }
            //  缓存存在则不处理
        }
    }

    /**
     * 缓存帖子下的所有评论id和根评论内容数据
     * @param eventNotice
     * @param topic
     * @param offset
     */
    @KafkaListener(topics = REPLY_INDEX_TOPIC)
    public void onIndexMessage(@Payload EventNotice eventNotice,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.OFFSET) long offset) {
        NoticePayload payload = eventNotice.getExtra();
        if (payload instanceof ReplyNotice replyIndexNotice) {
            int discussPostId = replyIndexNotice.getDiscussPostId();
            String replyIndexKey = LIST_REPLY_KEY + discussPostId;
            Set<String> indexSet = stringRedisTemplate.opsForZSet().range(replyIndexKey, 0, -1);
            if (indexSet == null || indexSet.isEmpty()) {
                //  如果缓存不存在，则执行重建缓存操作，将评论id进行缓存
                QueryWrapper<ReplyIndex> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("obj_id", discussPostId);
                queryWrapper.eq("root_rpid", 0);  //  只缓存根评论
                List<ReplyIndex> indexList = replyIndexMapper.selectList(queryWrapper);
                //  先重建根评论列表缓存，如果不存在结果则缓存错误对象防止缓存穿透
                rebuildReplyIndexCache(indexList, replyIndexKey);
                //  再重建评论内容缓存，如果不存在结果则缓存错误对象防止缓存穿透
                rebuildReplyDetailsCache(indexList);
            }
            //  缓存存在则不处理
        }
    }

    /**
     * 缓存该根评论下的评论内容数据
     * @param eventNotice
     * @param topic
     * @param offset
     */
    @KafkaListener(topics = REPLY_DETAILS_TOPIC)
    public void onContentMessage(@Payload EventNotice eventNotice,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.OFFSET) long offset) {
        NoticePayload payload = eventNotice.getExtra();
        if (payload instanceof ReplyNotice replyContentNotice) {
            //  TODO查询所有以该评论为根评论的评论内容和索引，并缓存
            int rpid = replyContentNotice.getReplyId();
            String rootReplyCacheKey = ROOT_REPLY_KEY + rpid;
            Set<String> rootReplyCache = stringRedisTemplate.opsForZSet().range(rootReplyCacheKey, 0, -1);
            if (rootReplyCache == null || rootReplyCache.isEmpty()) {
                //  如果缓存不存在，则执行重建缓存操作
                QueryWrapper<ReplyIndex> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("root_rpid", rpid);
                List<ReplyIndex> indexList = replyIndexMapper.selectList(queryWrapper);
                if (!indexList.isEmpty()) {
                    //  先重建根评论和子评论关系缓存
                    rebuildReplyIndexCache(indexList, rootReplyCacheKey);
                    //  再重建子评论内容缓存
                    rebuildReplyDetailsCache(indexList);
                }
            }
        }
    }

    private void rebuildReplyIndexCache(List<ReplyIndex> indexList, String cacheKey) {
        if (indexList == null || indexList.isEmpty()) {
            //  缓存错误对象防止缓存穿透
            stringRedisTemplate.opsForZSet().add(cacheKey, "-1", -1);
            stringRedisTemplate.expire(cacheKey, 60 * 10, TimeUnit.SECONDS);    //设置10分钟过期时间
            return;
        }
        Set<ZSetOperations.TypedTuple<String>> set = new HashSet<>();
        for (ReplyIndex replyIndex : indexList) {
            //  按照时间顺序储存
            long epochSecond = replyIndex.getCreateTime().atZone(ZoneId.systemDefault()).toEpochSecond();
            set.add(new DefaultTypedTuple<>(String.valueOf(replyIndex.getRpid()), (double) epochSecond));
            //  TODO:后面可以加上按照点赞热度排序
        }
        //  重建根评论索引缓存
        stringRedisTemplate.opsForZSet().add(cacheKey, set);
        stringRedisTemplate.expire(cacheKey, 60 * 10, TimeUnit.SECONDS);    //设置30s过期时间
    }

    private void rebuildReplyDetailsCache(List<ReplyIndex> indexList) {
        if (indexList == null || indexList.isEmpty()) {
            //  不存在评论区的评论，自然无法获取对应的评论内容，缓存空对象交给具体的单个评论查询接口处理，这里直接返回
            return;
        }
        //  多表联合查询，缓存根评论内容数据和索引数据
        List<ReplyContentVO> contentList = replyDetailsMapper.listByRpidIn(
                indexList.stream().map(ReplyIndex::getRpid).collect(Collectors.toList()));
        Map<String, ReplyContentVO> map = new HashMap<>();
        for (ReplyContentVO contentVO : contentList) {
            Integer rpid = contentVO.getRpid();
            String contentKey = CONTENT_REPLY_KEY + rpid;
            map.put(contentKey, contentVO);
        }
        //  批量写入缓存后加上过期时间
        contentRedisTemplate.opsForValue().multiSet(map);
        for (ReplyContentVO contentVO : contentList) {
            Integer rpid = contentVO.getRpid();
            String contentKey = CONTENT_REPLY_KEY + rpid;
            contentRedisTemplate.expire(contentKey, 60 * 10, TimeUnit.SECONDS);
        }
    }

    //  评论区缓存key，形式为section:reply:pattern:{discussPostId}，value值为JSON序列化的字符串，储存评论区基础信息
    public static final String REPLY_SEC_CACHE_KEY = "section:reply:pattern:";
    //  评论列表缓存key，形式为list:reply:pattern:{discussPostId},value值为zset的集合,member为reid，score可以是时间戳和点赞数
    public static final String LIST_REPLY_KEY = "list:reply:pattern:";
    //  评论内容key，形式为content:reply:pattern:{rpid}，value值为JSON序列化的字符串，储存评论内容和索引数据
    public static final String CONTENT_REPLY_KEY = "content:reply:pattern:";
    //  根评论关系key，形式为root:reply:pattern:{rpid},value值为zset的集合，member为子评论id，score可以是时间戳和点赞数
    public static final String ROOT_REPLY_KEY = "root:reply:pattern:";
}
