package com.nowcoder.community.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.common.utils.UserInfoHolder;
import com.nowcoder.community.domain.dto.DiscussPostDTO;
import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.entity.BusinessType;
import com.nowcoder.community.domain.entity.DiscussNotice;
import com.nowcoder.community.domain.po.Comment;
import com.nowcoder.community.domain.po.DiscussPost;
import com.nowcoder.community.domain.po.LikeCount;
import com.nowcoder.community.domain.po.User;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.domain.vo.DiscussPostVO;
import com.nowcoder.community.domain.vo.PageCommentVO;
import com.nowcoder.community.domain.vo.PosterVO;
import com.nowcoder.community.mapper.*;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussService;
import com.nowcoder.community.service.LikeRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscussServiceImpl extends ServiceImpl<DiscussMapper, DiscussPost> implements DiscussService {

    private final DiscussMapper discussMapper;
    private final UserMapper userMapper;
    private final CommentService commentService;
    private final ApplicationEventPublisher eventPublisher;
    private final LikeCountMapper likeCountMapper;

    @Override
    @Transactional
    public Result addDiscuss(String title, String content) {
        User user = UserInfoHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        DiscussPost discussPost = DiscussPost.builder()
                .title(title)
                .content(content)
                .userId(user.getId())
                .createTime(LocalDateTime.now())
                .build();
        discussMapper.insert(discussPost);
        //  TODO:异步创造帖子的点赞数记录?
        LikeCount likeCount = LikeCount.builder()
                .businessType(BusinessType.DISCUSS_POST.getValue())
                .likeCount(0)
                .dislikeCount(0)
                .targetId(discussPost.getId())
                .build();
        likeCountMapper.insert(likeCount);
        //  由于使用系统的事件发布器只能传输一个参数，所以必须要在调用侧进行构造事件
        eventPublisher.publishEvent(new DiscussNotice(DiscussNotice.ActionType.CREATED, discussPost));
        return Result.ok();
    }

    @Override
    public Result getDiscussById(int discussId, PageDTO pageDTO) {
        //  查询帖子内容
        QueryWrapper<DiscussPost> discussPostQueryWrapper = new QueryWrapper<>();
        discussPostQueryWrapper.eq("id", discussId);
        DiscussPost discussPost = discussMapper.selectOne(discussPostQueryWrapper);
        //  查询发帖人
        int userId = discussPost.getUserId();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id", userId);
        User user = userMapper.selectOne(userQueryWrapper);
        PosterVO posterVO = BeanUtil.copyProperties(user, PosterVO.class);
        //  查询评论内容
        PageCommentVO data = null;
        if (pageDTO != null) {
            data = (PageCommentVO) commentService.getComment(discussId, pageDTO).getData();
        }
        DiscussPostVO discussPostVO = DiscussPostVO.builder()
                .title(discussPost.getTitle())
                .content(discussPost.getContent())
                .posterVO(posterVO)
                .createTime(discussPost.getCreateTime())
                .comment(data)
                .build();
        return Result.ok(discussPostVO);
    }

    @Transactional
    public Result updateDiscuss(DiscussPost discussPost) {
        discussMapper.updateById(discussPost);
        eventPublisher.publishEvent(new DiscussNotice(DiscussNotice.ActionType.UPDATED, discussPost));
        return Result.ok();
    }

    @Transactional
    public Result deleteDiscuss(int discussId) {
        discussMapper.deleteById(discussId);
        //  TODO:异步删除帖子的点赞数记录，删除点赞记录
        likeCountMapper.deleteById(discussId);
        eventPublisher.publishEvent(new DiscussNotice(DiscussNotice.ActionType.DELETED,
                DiscussPost.builder().id(discussId).build()));
        return Result.ok();
    }
}
