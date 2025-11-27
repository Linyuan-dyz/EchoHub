package com.nowcoder.community.domain.vo;

import com.nowcoder.community.domain.dto.CommentDTO;
import com.nowcoder.community.domain.po.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageCommentVO {

    List<Comment> commentList;

    long total;
}
