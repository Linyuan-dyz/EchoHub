package com.nowcoder.community.common.schdule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.nowcoder.community.common.kafka.EventPublisher;
import com.nowcoder.community.domain.entity.BusinessType;
import com.nowcoder.community.domain.po.Comment;
import com.nowcoder.community.domain.po.LikeCount;
import com.nowcoder.community.domain.po.LikeRelation;
import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.mapper.DiscussMapper;
import com.nowcoder.community.mapper.LikeCountMapper;
import com.nowcoder.community.mapper.LikeRelationMapper;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.LikeRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


import static com.nowcoder.community.common.constant.RedisConstants.*;

@Component
@RequiredArgsConstructor
public class likeSchedule {
    private final StringRedisTemplate redisTemplate;
    private final LikeRelationMapper likeRelationMapper;
    private final LikeRelationService likeRelationService;
    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final EventPublisher eventPublisher;
    private final LikeCountMapper likeCountMapper;

    /**
     * 定时任务：每隔2分钟将Redis中评论的点赞数据同步到数据库
     * 由于点赞系统做的是增量处理，所以每次只需要将脏评论的点赞数据添加，取消赞数据删除
     * TODO:当前还没有加上数据读取锁，可能会存在并发问题，需要保证数据读取之后立即删除，不能被其他线程更改点赞数据
     */
//    @Transactional
//    @Scheduled(fixedDelay = 1000 * 10)
//    public void flushLikeData() {
//        Set<String> members = redisTemplate.opsForSet().members(DIRTY_COMMENT_KEY);
//        Map<String, Integer> likeCountMap = new HashMap<>();
//        Map<String, List<Integer>> likeUserMap = new HashMap<>();
//        Map<String, List<Integer>> unLikeUserMap = new HashMap<>();
//        //  获取评论ID、点赞数量、点赞用户增量、取消点赞用户增量，获取后直接将数据删除，防止重复获取
//        if (members != null && !members.isEmpty()) {
//            for (String commentIdStr : members) {
//                getCountDataFromRedis(commentIdStr, COMMENT_LIKE_COUNT + commentIdStr, likeCountMap);
//                getUserDataFromRedis(commentIdStr, COMMENT_LIKE_USER_KEY + commentIdStr, likeUserMap);
//                getUserDataFromRedis(commentIdStr, COMMENT_UNLIKE_USER_KEY + commentIdStr, unLikeUserMap);
//            }
//        }
//
//        //  批量更新评论点赞数量
//        if (!likeCountMap.isEmpty()) {
//            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
//            queryWrapper.in("id", likeCountMap.keySet());
//            queryWrapper.select("id", "like_count");
//            List<Comment> commentList = commentMapper.selectList(queryWrapper);
//            for (String commentIdStr : likeCountMap.keySet()) {
//                Comment comment = commentList.remove(0);
//                comment.setLikeCount(comment.getLikeCount() + likeCountMap.get(commentIdStr));
//                commentList.add(comment);
//            }
//            commentService.updateBatchById(commentList);
//        }
//
//        if (!likeUserMap.isEmpty()) {
//            //  批量更新点赞用户
//            List<LikeRelation> likeRelations = new ArrayList<>();
//            for (String commentIdStr : likeUserMap.keySet()) {
//                int commentId = Integer.parseInt(commentIdStr);
//                List<Integer> userIds = likeUserMap.get(commentIdStr);
//                for (Integer userId : userIds) {
//                    LikeRelation likeRelation = LikeRelation.builder()
//                            .userId(userId)
//                            .targetId(commentId)
//                            .targetType(LikeRelation.COMMENT_TYPE)
//                            .createTime(LocalDateTime.now())
//                            .build();
//                    likeRelations.add(likeRelation);
//                }
//            }
//            likeRelationService.saveBatch(likeRelations);
//        }
//
//        //  批量更新取消点赞用户
//        if (!unLikeUserMap.isEmpty()) {
//            List<LikeRelation> unLikeRelations = new ArrayList<>();
//            for (String commentIdStr : unLikeUserMap.keySet()) {
//                int commentId = Integer.parseInt(commentIdStr);
//                List<Integer> userIds = unLikeUserMap.get(commentIdStr);
//                for (Integer userId : userIds) {
//                    LikeRelation unLikeRelation = LikeRelation.builder()
//                            .userId(userId)
//                            .targetId(commentId)
//                            .targetType(LikeRelation.COMMENT_TYPE)
//                            .build();
//                    unLikeRelations.add(unLikeRelation);
//                }
//            }
//            likeRelationMapper.batchDeleteByConditions(unLikeRelations);
//        }
//
//
//        //  清除脏评论集合，交给定时任务实现
//        redisTemplate.delete(DIRTY_COMMENT_KEY);
//    }
//
//    /**
//     * 从Redis中获取用户数据，获取后将数据删除
//     * TODO:当前还没有加上数据读取锁，可能会存在并发问题，需要保证数据读取之后立即删除，不能被其他线程更改点赞数据
//     * @param targetId
//     * @param key
//     * @param map
//     */
//    public void getUserDataFromRedis(String targetId, String key, Map<String, List<Integer>> map) {
//        Set<String> userDataSet = redisTemplate.opsForSet().members(key);
//        if (userDataSet != null && !userDataSet.isEmpty()) {
//            for (String userData : userDataSet) {
//                int userId = Integer.parseInt(userData);
//                if (map.containsKey(targetId)) {
//                    List<Integer> list = map.get(targetId);
//                    list.add(userId);
//                } else {
//                    List<Integer> list = new ArrayList<>();
//                    list.add(userId);
//                    map.put(targetId, list);
//                }
//            }
//        }
//        redisTemplate.delete(key);
//    }
//
//    /**
//     * 从redis中获取点赞数量，获取后将数据删除
//     * TODO:当前还没有加上数据读取锁，可能会存在并发问题，需要保证数据读取之后立即删除，不能被其他线程更改点赞数据
//     * @param key
//     * @param map
//     */
//    public void getCountDataFromRedis(String targetIdStr, String key, Map<String, Integer> map) {
//        String countStr = redisTemplate.opsForValue().get(key);
//        int likeCount = countStr == null ? 0 : Integer.parseInt(countStr);
//        map.put(targetIdStr, likeCount);
//        redisTemplate.delete(key);
//    }

    @Scheduled(fixedDelay = 1000 * 20)
    public void likeDiscussCountSchedule() {
        likeCountUpdate(BusinessType.DISCUSS_POST);
    }

    @Scheduled(fixedDelay = 1000 * 20)
    public void likeCommentCountSchedule() {
        likeCountUpdate(BusinessType.COMMENT);
    }

    private void likeCountUpdate(BusinessType businessType) {
        //  取出脏ID集合，并转换为int类型
        String dirtyDiscussLikeKey = DIRTY_LIKE_TARGET_KEY + businessType.getValue();
        Set<String> discussIds = redisTemplate.opsForSet().members(dirtyDiscussLikeKey);
        if (discussIds == null || discussIds.isEmpty()) {
            return;
        }
        Set<Integer> discussIdSet = new HashSet<>();
        for (String discussIdStr : discussIds) {
            int discussId = Integer.parseInt(discussIdStr);
            discussIdSet.add(discussId);
        }
        //  批量更新脏ID对应的点赞数量
        String discussLikeCountKeyPrefix = TARGET_LIKE_AND_DISLIKE_COUNT + businessType.getValue() + ":";
        List<LikeCount> list = new ArrayList<>();
        for (Integer discussId : discussIdSet) {
            String discussLikeCountKey = discussLikeCountKeyPrefix + discussId;
            Object likeCount = redisTemplate.opsForHash().get(discussLikeCountKey, "like");
            Object dislikeCount = redisTemplate.opsForHash().get(discussLikeCountKey, "dislike");
            LikeCount count = LikeCount.builder()
                    .businessType(businessType.getValue())
                    .likeCount(likeCount != null ? Integer.parseInt(likeCount.toString()) : 0)
                    .dislikeCount(dislikeCount != null ? Integer.parseInt(dislikeCount.toString()) : 0)
                    .targetId(discussId)
                    .build();
            list.add(count);
        }
        likeCountMapper.upsertDeltaBatch(list);
        redisTemplate.delete(dirtyDiscussLikeKey);
    }
}
