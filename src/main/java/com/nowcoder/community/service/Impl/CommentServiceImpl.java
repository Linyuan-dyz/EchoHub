package com.nowcoder.community.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.common.kafka.EventPublisher;
import com.nowcoder.community.common.utils.UserInfoHolder;
import com.nowcoder.community.domain.dto.CommentDTO;
import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.entity.BusinessType;
import com.nowcoder.community.domain.entity.SysNotice;
import com.nowcoder.community.domain.po.*;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.domain.vo.PageCommentVO;
import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.mapper.DiscussMapper;
import com.nowcoder.community.mapper.LikeCountMapper;
import com.nowcoder.community.mapper.LikeRelationMapper;
import com.nowcoder.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.nowcoder.community.common.constant.RedisConstants.*;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final DiscussMapper discussMapper;
    private final StringRedisTemplate redisTemplate;
    private final LikeRelationMapper likeRelationMapper;
    private final EventPublisher eventPublisher;
    private final LikeCountMapper likeCountMapper;

    @Override
    public Result getComment(int discussPostId, PageDTO pageDTO) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_id", discussPostId);
        // 通过分页查询评论
        Page<Comment> page = new Page<>(pageDTO.getCurrent(), pageDTO.getLimit());
        commentMapper.selectPage(page, wrapper);
        long total = page.getTotal();
        PageCommentVO pageCommentVO = PageCommentVO.builder()
                .commentList(page.getRecords())
                .total(total)
                .build();
        return Result.ok(pageCommentVO);
    }

    @Transactional
    @Override
    public Result addComment(int discussPostId, CommentDTO commentDTO) {
        User user = UserInfoHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        int userId = user.getId();
        //  检查帖子是否存在并添加评论数
        DiscussPost discussPost = discussMapper.selectOne(new QueryWrapper<DiscussPost>().eq("id", discussPostId));
        if (discussPost == null) {
            return Result.fail("该帖子不存在");
        }
        discussPost.setCommentCount(discussPost.getCommentCount() + 1);
        discussMapper.updateById(discussPost);
        //  添加评论
        Comment comment = BeanUtil.copyProperties(commentDTO, Comment.class);
        comment.setUserId(userId);
        commentMapper.insert(comment);
        //  TODO:异步添加评论的点赞数记录
        LikeCount likeCount = LikeCount.builder()
                .targetId(comment.getId())
                .likeCount(0)
                .dislikeCount(0)
                .businessType(BusinessType.COMMENT.getValue())
                .build();
        likeCountMapper.insert(likeCount);
        return Result.ok();
    }

    @Transactional
    @Override
    public Result deleteComment(int commentId, int discussPostId) {
        //  检查帖子是否存在并减少评论数
        DiscussPost discussPost = discussMapper.selectOne(new QueryWrapper<DiscussPost>().eq("id", discussPostId));
        discussPost.setCommentCount(discussPost.getCommentCount() - 1);
        discussMapper.updateById(discussPost);
        //  删除评论
        commentMapper.deleteById(commentId);
        //TODO:异步删除评论点赞数记录，异步删除所有点赞记录
        likeCountMapper.deleteById(commentId);
        return Result.ok();
    }

    /**
     * 点赞评论（做增量处理，不要去拉取所有的点赞用户，而是记录所有点赞和取消赞的增量，之后批量写入）
     * @param commentId
     * @return
     */
//    @Transactional
//    @Override
//    public Result likeComment(int commentId) {
//        if (UserInfoHolder.getUser() == null) {
//            return Result.fail("请先登录");
//        }
//        int userId = UserInfoHolder.getUser().getId();
//        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
//        commentQueryWrapper.eq("id", commentId);
//        Comment comment = commentMapper.selectOne(commentQueryWrapper);
//        //  标记当前评论为脏评论，在定时任务中处理
//        redisTemplate.opsForSet().add(DIRTY_COMMENT_KEY, String.valueOf(commentId));
//
//        //  检查当前帖子点赞数量是否存在，不存在则去数据库获取点赞数量和点赞用户
//        if (!isLikeCountInRedis(commentId)) {
//            //  去数据库查询点赞数量，并同步到redis
//            if (comment == null) {
//                return Result.fail("评论不存在");
//            }
//            int likeCount = comment.getLikeCount();
//            redisTemplate.opsForValue().set(COMMENT_LIKE_COUNT + commentId, String.valueOf(likeCount));
//        }
//        if (likeComment(commentId, userId)) {
//            //  取消点赞
//            redisTemplate.opsForValue().decrement(COMMENT_LIKE_COUNT + commentId);
//            //  如果在点赞列表中，则移除
//            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(COMMENT_LIKE_USER_KEY + commentId, String.valueOf(userId)))) {
//                redisTemplate.opsForSet().remove(COMMENT_LIKE_USER_KEY + commentId, String.valueOf(userId));
//            } else {
//                //  如果不在点赞列表中，则添加到点踩列表中
//                redisTemplate.opsForSet().add(COMMENT_UNLIKE_USER_KEY + commentId, String.valueOf(userId));
//            }
//            return Result.ok();
//        } else {
//            //  点赞成功
//            redisTemplate.opsForSet().add(COMMENT_LIKE_USER_KEY + commentId, String.valueOf(userId));
//            redisTemplate.opsForValue().increment(COMMENT_LIKE_COUNT + commentId);
//            eventPublisher.publishLike(userId, comment.getTargetId(), SysNotice.ObjectType.COMMENT, commentId,SysNotice.EventType.COMMENT, null);
//            return Result.ok();
//        }
//    }
//
//    @Override
//    public Result likeCount(int commentId) {
//        //  检查redis中是否有点赞数据量
//        if (isLikeCountInRedis(commentId)) {
//            return Result.ok(redisTemplate.opsForValue().get(COMMENT_LIKE_COUNT + commentId));
//        }
//        //  如果没有，则去数据库查询
//        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("id", commentId);
//        Comment comment = commentMapper.selectOne(queryWrapper);
//        if (comment == null) {
//            return Result.fail("评论不存在");
//        } else {
//            int likeCount = comment.getLikeCount();
//            return Result.ok(likeCount);
//        }
//    }
//
//    @Override
//    public Result likeStatus(int commentId) {
//        if (UserInfoHolder.getUser() == null) {
//            return Result.fail("请先登录");
//        }
//        int userId = UserInfoHolder.getUser().getId();
//        boolean isLike = likeComment(commentId, userId);
//        return Result.ok(isLike);
//    }
//
//    private boolean isLikeCountInRedis(int commentId) {
//        return redisTemplate.opsForValue().get(COMMENT_LIKE_COUNT + commentId) != null;
//    }
//
//    private boolean likeComment(int commentId, int userId) {
//        //  如果redis中存在当前评论的点赞数据
//        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(COMMENT_LIKE_USER_KEY + commentId, String.valueOf(userId)))) {
//            return true;
//        }
//        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(COMMENT_UNLIKE_USER_KEY + commentId, String.valueOf(userId)))) {
//            return false;
//        }
//        //  如果没有，则去数据库查询
//        QueryWrapper<LikeRelation> likeRelationQueryWrapper = new QueryWrapper<>();
//        likeRelationQueryWrapper.eq("target_id", commentId).eq("user_id", userId);
//        LikeRelation likeRelation = likeRelationMapper.selectOne(likeRelationQueryWrapper);
//        return likeRelation != null;
//    }
}
