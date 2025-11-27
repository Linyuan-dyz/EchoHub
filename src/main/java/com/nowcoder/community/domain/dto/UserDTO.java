package com.nowcoder.community.domain.dto;

import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Description("用户更新数据DTO")
public class UserDTO {

    private String password;

    private String email;

    private String phone;

    private String headerURL;
}
