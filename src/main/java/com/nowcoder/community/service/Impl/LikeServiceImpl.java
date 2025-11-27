package com.nowcoder.community.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nowcoder.community.common.utils.UserInfoHolder;
import com.nowcoder.community.domain.entity.BusinessType;
import com.nowcoder.community.domain.entity.LikeNotice;
import com.nowcoder.community.domain.entity.LikeStatus;
import com.nowcoder.community.domain.po.LikeCount;
import com.nowcoder.community.domain.po.LikeRelation;
import com.nowcoder.community.domain.po.User;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.mapper.LikeCountMapper;
import com.nowcoder.community.mapper.LikeRelationMapper;
import com.nowcoder.community.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.nowcoder.community.common.constant.RedisConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {

    private final LikeCountMapper likeCountMapper;
    private final LikeRelationMapper likeRelationMapper;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result operate(int targetId, BusinessType businessType, LikeStatus likeStatus) {
        User user = UserInfoHolder.getUser();
        if (user == null) {
            log.error("用户未登录，无法进行操作");
            return Result.fail("用户未登录，无法进行操作");
        }
        refreshRecentLikeInCache(businessType, user);
        int busType = businessType.getValue();
        String recentUserLikeKey = RECENT_USER_LIKE  + user.getId() + ":" + busType;
        String targetCountKey = TARGET_LIKE_AND_DISLIKE_COUNT + busType + ":" + targetId;
        Double score = redisTemplate.opsForZSet().score(recentUserLikeKey, String.valueOf(targetId));
        boolean liked = score != null;
        refreshTargetLikeCount(targetId, targetCountKey, busType);
        //不需要给点赞加锁，因为redis的操作本身就是原子性的，自增和自减操作可以保证原子性
        if (liked) {
            //  已点赞状态，需要处理用户最近点赞列表、目标点赞数缓存、数据库点赞关系、点赞数
            //  目标点赞人列表依靠数据库点赞关系维护，一般不需要拉到缓存中
            redisTemplate.opsForZSet().remove(recentUserLikeKey, String.valueOf(targetId)); //处理最近点赞列表
            redisTemplate.opsForHash().increment(targetCountKey, "like", -1); //处理目标点赞数缓存
            //TODO:异步删除点赞关系，定时任务同步点赞数
            LikeNotice likeNotice = LikeNotice.builder()
                    .userId(user.getId())
                    .likeStatus(likeStatus)
                    .operateType(LikeNotice.OperateType.CANCEL)
                    .targetType(busType)
                    .targetId(targetId)
                    .createTime(LocalDateTime.now())
                    .build();
            publisher.publishEvent(likeNotice);
        } else {
            //  未点赞状态
            long epochSecond = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();  //添加最近点赞列表
            redisTemplate.opsForZSet().add(recentUserLikeKey, String.valueOf(targetId), (double) epochSecond);
            redisTemplate.expire(recentUserLikeKey, RECENT_LIKE_EXPIRE, TimeUnit.MILLISECONDS);
            redisTemplate.opsForHash().increment(targetCountKey, "like", 1); //处理目标点赞数缓存
            //TODO:异步添加点赞关系，定时任务同步点赞数
            LikeNotice likeNotice = LikeNotice.builder()
                    .userId(user.getId())
                    .likeStatus(likeStatus)
                    .operateType(LikeNotice.OperateType.CREATE)
                    .targetType(busType)
                    .targetId(targetId)
                    .createTime(LocalDateTime.now())
                    .build();
            publisher.publishEvent(likeNotice);
        }
        String dirtyTargetKey = DIRTY_LIKE_TARGET_KEY + busType;
        redisTemplate.opsForSet().add(dirtyTargetKey, String.valueOf(targetId));
        return Result.ok();
    }

    @Override
    public Result getStatus(int targetId, BusinessType businessType, LikeStatus likeStatus) {
        User user = UserInfoHolder.getUser();
        if (user == null) {
            log.error("用户未登录，无法进行操作");
            return Result.fail("用户未登录，无法进行操作");
        }
        refreshRecentLikeInCache(businessType, user);
        int busType = businessType.getValue();
        String recentUserLikeKey = RECENT_USER_LIKE  + user.getId() + ":" + busType;
        Double score = redisTemplate.opsForZSet().score(recentUserLikeKey, String.valueOf(targetId));
        return score == null ? Result.ok(false) : Result.ok(true);
    }

    @Override
    public Result getLikeCount(int targetId, BusinessType businessType, LikeStatus likeStatus) {
        //  TODO:加一个布隆过滤器或空对象防止缓存击穿
        int busType = businessType.getValue();
        String targetCountKey = TARGET_LIKE_AND_DISLIKE_COUNT + busType + ":" + targetId;
        Map<String, String> targetCountMap = refreshTargetLikeCount(targetId, targetCountKey, busType);
        return Result.ok(Integer.parseInt(targetCountMap.get("like")));
    }

    @Override
    public Result getUserLikeList(BusinessType businessType) {
        User user = UserInfoHolder.getUser();
        if (user == null) {
            log.error("用户未登录，无法进行操作");
            return Result.fail("用户未登录，无法进行操作");
        }
        refreshRecentLikeInCache(businessType, user);
        int busType = businessType.getValue();
        String recentUserLikeKey = RECENT_USER_LIKE  + user.getId() + ":" + busType;
        //  获取用户最近点赞列表，只需要得到目标ID集合即可
        Set<String> recentLike = redisTemplate.opsForZSet().range(recentUserLikeKey, 0, -1);
        return Result.ok(recentLike);
    }

    /**
     * 检查缓存中是否有点赞缓存，如果没有则从数据库加载用户点赞列表写回Redis
     * TODO:该方法会被多处调用，考虑多线程并发问题？
     * @param businessType
     * @param user
     */
    private void refreshRecentLikeInCache(BusinessType businessType, User user) {
        int busType = businessType.getValue();
        String recentUserLikeKey = RECENT_USER_LIKE  + user.getId() + ":" + busType;
        Set<String> recentLike = redisTemplate.opsForZSet().range(recentUserLikeKey, 0, -1);
        if (recentLike == null || recentLike.isEmpty()) {
            //  从数据库中加载用户点赞列表，并写回Redis
            QueryWrapper<LikeRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("target_type", busType);
            queryWrapper.eq("user_id", user.getId());
            queryWrapper.select("target_id", "create_time");
            //  TODO:这里还需要注意内存溢出问题，最好分页加载数据，批量分词写入redis中
            List<LikeRelation> list = likeRelationMapper.selectList(queryWrapper);
            Set<ZSetOperations.TypedTuple<String>> set = new HashSet<>();
            for (LikeRelation likeRelation : list) {
                //  将目标ID作为key，当前时间戳作为score写入Redis有序集合中
                long epochSecond = likeRelation.getCreateTime().atZone(ZoneId.systemDefault()).toEpochSecond();
                set.add(new DefaultTypedTuple<>(String.valueOf(likeRelation.getTargetId()), (double) epochSecond));
            }
            if (!set.isEmpty()) {
                redisTemplate.opsForZSet().add(recentUserLikeKey, set);
                redisTemplate.expire(recentUserLikeKey, RECENT_LIKE_EXPIRE, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * 判断点赞量数据是否存在，如果不存在则去数据库中查找对应目标的点赞/点踩数据，返回点赞/点踩数据字符串
     * @param targetId
     * @param targetCountKey
     * @param busType
     * @return
     */
    private Map<String, String> refreshTargetLikeCount(int targetId, String targetCountKey, int busType) {
        Map<Object, Object> countMapInCache = redisTemplate.opsForHash().entries(targetCountKey);
        Map<String, String> countMap = new HashMap<>();
        if (countMapInCache.isEmpty()) {
            QueryWrapper<LikeCount> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("business_type", busType);
            queryWrapper.eq("target_id", targetId);
            LikeCount likeCount = likeCountMapper.selectOne(queryWrapper);
            //  在创建帖子和评论时已经做了初始化，可以在数据库中查到数据
            countMap.put("like", String.valueOf(likeCount.getLikeCount()));
            countMap.put("dislike", String.valueOf(likeCount.getDislikeCount()));
            redisTemplate.opsForHash().putAll(targetCountKey, countMap);
            redisTemplate.expire(targetCountKey, TARGET_COUNT_EXPIRE, TimeUnit.MILLISECONDS);
        } else {
            countMap.put("like", String.valueOf(countMapInCache.get("like")));
            countMap.put("dislike", String.valueOf(countMapInCache.get("dislike")));
        }
        return countMap;
    }

}
