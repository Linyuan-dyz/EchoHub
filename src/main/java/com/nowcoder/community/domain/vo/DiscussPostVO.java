package com.nowcoder.community.domain.vo;

import com.nowcoder.community.domain.po.DiscussPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscussPostVO {

    private PosterVO posterVO;

    private String title;

    private String content;

    private LocalDateTime createTime;

    private PageCommentVO comment;
}
