package com.nowcoder.community.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LetterListVO {

    public List<LetterVO> letters;

    public Long total;
}
