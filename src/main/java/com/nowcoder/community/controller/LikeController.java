package com.nowcoder.community.controller;

import com.nowcoder.community.domain.entity.BusinessType;
import com.nowcoder.community.domain.entity.LikeStatus;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/like/discussPost/{discussPostId}")
    public Result likeDiscussPost(@PathVariable int discussPostId) {
        return likeService.operate(discussPostId, BusinessType.DISCUSS_POST, LikeStatus.LIKE);
    }

    @PostMapping("/dislike/discussPost/{discussPostId}")
    public Result dislikeDiscussPost(@PathVariable int discussPostId) {
        return likeService.operate(discussPostId, BusinessType.DISCUSS_POST, LikeStatus.DISLIKE);
    }

    @PostMapping("/like/comment/{commentId}")
    public Result likeComment(@PathVariable int commentId) {
        return likeService.operate(commentId, BusinessType.COMMENT, LikeStatus.LIKE);
    }

    @PostMapping("/dislike/comment/{commentId}")
    public Result dislikeComment(@PathVariable int commentId) {
        return likeService.operate(commentId, BusinessType.COMMENT, LikeStatus.DISLIKE);
    }

    /**
     * 获取当前用户的帖子点赞状态
     * @param discussPostId
     * @return
     */
    @GetMapping("/like/discussPost/{discussPostId}")
    public Result getDiscussPostLikeStatus(@PathVariable int discussPostId) {
        return likeService.getStatus(discussPostId, BusinessType.DISCUSS_POST, LikeStatus.LIKE);
    }

    /**
     * 获取当前用户的帖子点踩状态
     * @param discussPostId
     * @return
     */
    @GetMapping("/dislike/discussPost/{discussPostId}")
    public Result getDiscussPostDislikeStatus(@PathVariable int discussPostId) {
        return likeService.getStatus(discussPostId, BusinessType.DISCUSS_POST, LikeStatus.DISLIKE);
    }

    /**
     * 获取当前用户的评论点赞状态
     * @param commentId
     * @return
     */
    @GetMapping("/like/comment/{commentId}")
    public Result getCommentLikeStatus(@PathVariable int commentId) {
        return likeService.getStatus(commentId, BusinessType.COMMENT, LikeStatus.LIKE);
    }

    /**
     * 获取当前用户的评论点踩状态
     * @param commentId
     * @return
     */
    @GetMapping("/dislike/comment/{commentId}")
    public Result getCommentDislikeStatus(@PathVariable int commentId) {
        return likeService.getStatus(commentId, BusinessType.COMMENT, LikeStatus.DISLIKE);
    }

    /**
     * 获取帖子点赞数量
     * @param discussPostId
     * @return
     */
    @GetMapping("/like/count/discussPost/{discussPostId}")
    public Result getDiscussPostLikeCount(@PathVariable int discussPostId) {
        return likeService.getLikeCount(discussPostId, BusinessType.DISCUSS_POST, LikeStatus.LIKE);
    }

//    /**
//     * 获取帖子点踩数量
//     * @param discussPostId
//     * @return
//     */
//    @GetMapping("/dislike/count/discussPost/{discussPostId}")
//    public Result getDiscussPostDislikeCount(@PathVariable int discussPostId) {
//        return likeService.getDislikeCount(discussPostId, BusinessType.DISCUSS_POST, LikeStatus.DISLIKE);
//    }

    /**
     * 获取评论点赞数量
     * @param commentId
     * @return
     */
    @GetMapping("/like/count/comment/{commentId}")
    public Result getCommentLikeCount(@PathVariable int commentId) {
        return likeService.getLikeCount(commentId, BusinessType.COMMENT, LikeStatus.LIKE);
    }

//    /**
//     * 获取评论点踩数量
//     * @param commentId
//     * @return
//     */
//    @GetMapping("/dislike/count/comment/{commentId}")
//    public Result getCommentDislikeCount(@PathVariable int commentId) {
//        return likeService.getDislikeCount(commentId, BusinessType.COMMENT, LikeStatus.DISLIKE);
//    }

    @GetMapping("/like/user/discussPost")
    public Result getUserLikeCount() {
        return likeService.getUserLikeList(BusinessType.DISCUSS_POST);
    }

    @GetMapping("/like/user/comment")
    public Result getUserCommentLikeCount() {
        return likeService.getUserLikeList(BusinessType.COMMENT);
    }

}
