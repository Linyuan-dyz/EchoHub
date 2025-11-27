package com.nowcoder.community.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {

    private UserVO userVO;

    private String accessToken;

}
