package com.nowcoder.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.domain.dto.CommentDTO;
import com.nowcoder.community.domain.dto.PageDTO;
import com.nowcoder.community.domain.po.Comment;
import com.nowcoder.community.domain.response.Result;

public interface CommentService extends IService<Comment> {
    Result getComment(int discussPostId, PageDTO pageDTO);

    Result addComment(int discussPostId, CommentDTO commentDTO);

    Result deleteComment(int commentId, int discussPostId);

//    Result likeComment(int commentId);
//
//    Result likeCount(int commentId);
//
//    Result likeStatus(int commentId);
}
