package com.nowcoder.community.domain.vo;

import com.nowcoder.community.domain.po.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LetterVO {

    public PosterVO poster;

    public Message message;

}
