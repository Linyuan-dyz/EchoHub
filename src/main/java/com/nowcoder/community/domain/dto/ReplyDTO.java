package com.nowcoder.community.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyDTO {

    Integer postId;

    Integer rootRpid;

    Integer parentRpid;

    Integer userId;

    String content;
}
