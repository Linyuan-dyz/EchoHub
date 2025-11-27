package com.nowcoder.community.controller;

import com.nowcoder.community.domain.dto.CommentDTO;
import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.po.Comment;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/details/{discussPostId}")
    public Result getComment(@PathVariable int discussPostId, @RequestParam PageDTO pageDTO) {
        return commentService.getComment(discussPostId, pageDTO);
    }

    @PostMapping("/add/{discussPostId}")
    public Result addComment(@PathVariable int discussPostId, CommentDTO commentDTO) {
        return commentService.addComment(discussPostId, commentDTO);
    }

    @DeleteMapping("/delete/{discussPostId}")
    public Result deleteComment(@RequestParam int commentId, @PathVariable int discussPostId) {
        return commentService.deleteComment(commentId, discussPostId);
    }

//    @PostMapping("/like/{commentId}")
//    public Result likeComment(@PathVariable int commentId) {
//        return commentService.likeComment(commentId);
//    }
//
//    @GetMapping("/like/count/{commentId}")
//    public Result likeCount(@PathVariable int commentId) {
//        return commentService.likeCount(commentId);
//    }
//
//    @GetMapping("/like/status/{commentId}")
//    public Result likeStatus(@PathVariable int commentId) {
//        return commentService.likeStatus(commentId);
//    }
}
