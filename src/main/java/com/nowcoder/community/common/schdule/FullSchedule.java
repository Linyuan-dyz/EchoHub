package com.nowcoder.community.common.schdule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.domain.entity.BusinessType;
import com.nowcoder.community.domain.entity.PostDocument;
import com.nowcoder.community.domain.po.Comment;
import com.nowcoder.community.domain.po.DiscussPost;
import com.nowcoder.community.domain.po.LikeCount;
import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.mapper.DiscussMapper;
import com.nowcoder.community.mapper.LikeCountMapper;
import com.nowcoder.community.service.DiscussService;
import com.nowcoder.community.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullSchedule {
    private final DiscussMapper discussMapper;
    private final PostService postService;
    private final CommentMapper commentMapper;
    private final LikeCountMapper likeCountMapper;

    @Async
//    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void fullConstruct() {
        log.info("fullConstruct");
        int pageNum = 1;
        int pageSize = 100;
        while (true) {
            Page<DiscussPost> page = new Page<>(pageNum, pageSize);
            log.info("current page: {} total page: {}", pageNum, page.getTotal());
            List<DiscussPost> result = discussMapper.selectList(page, new QueryWrapper<>());
            if (result.isEmpty()) {
                break;
            }
            List<PostDocument> docs = new ArrayList<>();
            for (DiscussPost discussPost : result) {
                PostDocument postDocument = PostDocument.builder()
                        .id(discussPost.getId())
                        .title(discussPost.getTitle())
                        .content(discussPost.getContent())
                        .type(discussPost.getType())
                        .score(discussPost.getScore())
                        .userId(discussPost.getUserId())
                        .createTime(discussPost.getCreateTime())
                        .commentCount(discussPost.getCommentCount())
                        .status(discussPost.getStatus())
                        .build();
                docs.add(postDocument);
            }
            postService.bulk(docs);
            pageNum++;
        }
        log.info("fullConstruct finished");
    }

    @Async
//    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void fullCommentCountUpdate() {
        log.info("fullCommentCountUpdate started");
        int pageNum = 1;
        int pageSize = 100;
        while (true) {
            List<LikeCount> list = new ArrayList<>();
            Page<Comment> page = new Page<>(pageNum, pageSize);
            log.info("Comment:  current page: {} total page: {}", pageNum, page.getTotal());
            List<Comment> result = commentMapper.selectList(page, new QueryWrapper<>());
            if (result.isEmpty()) {
                break;
            }
            for (Comment comment : result) {
                LikeCount likeCount = LikeCount.builder()
                        .businessType(BusinessType.COMMENT.getValue())
                        .likeCount(0)
                        .dislikeCount(0)
                        .targetId(comment.getId())
                        .build();
                list.add(likeCount);
            }
            likeCountMapper.insertOrUpdate(list);
            pageNum++;
        }
        log.info("fullCommentCountUpdate finished");
    }

    @Async
//    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void fullDiscussPostCountUpdate() {
        log.info("fullDiscussPostCountUpdate started");
        int pageNum = 1;
        int pageSize = 100;
        while (true) {
            List<LikeCount> list = new ArrayList<>();
            Page<DiscussPost> page = new Page<>(pageNum, pageSize);
            log.info("DiscussPost:  current page: {} total page: {}", pageNum, page.getTotal());
            List<DiscussPost> result = discussMapper.selectList(page, new QueryWrapper<>());
            if (result.isEmpty()) {
                break;
            }
            for (DiscussPost discussPost : result) {
                LikeCount likeCount = LikeCount.builder()
                        .businessType(BusinessType.DISCUSS_POST.getValue())
                        .likeCount(0)
                        .dislikeCount(0)
                        .targetId(discussPost.getId())
                        .build();
                list.add(likeCount);
            }
            likeCountMapper.insertOrUpdate(list);
            pageNum++;
        }
        log.info("fullDiscussPostCountUpdate finished");
    }
}
