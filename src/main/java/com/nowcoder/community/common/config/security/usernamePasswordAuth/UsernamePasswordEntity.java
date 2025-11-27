package com.nowcoder.community.common.config.security.usernamePasswordAuth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsernamePasswordEntity {

    private String username;

    private String password;
}
