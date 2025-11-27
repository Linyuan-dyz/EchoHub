package com.nowcoder.community.domain.vo;

import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO {

    @Description("作为JWT令牌生成参数之一")
    private String username;

    @Description("作为JWT令牌生成参数之一")
    private String email;

    private String phone;

    private String headerUrl;

    private LocalDateTime createTime;

}
