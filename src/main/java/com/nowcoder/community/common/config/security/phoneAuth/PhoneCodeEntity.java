package com.nowcoder.community.common.config.security.phoneAuth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneCodeEntity {
    private String phone;

    private String code;

}
