package com.nowcoder.community.common.constant;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JWTConstants {

    // 用于给Jwt令牌签名校验的秘钥
    public static final String JWT_SIGN_KEY = "1145141919810";
    // 存放用户名的jwt key
    public static final String USER_NAME_KEY = "userName";
    // 存放用户邮箱的jwt key
    public static final String USER_EMAIL_KEY = "userEmail";
    // token发行者
    public static final String ISS = "Double-Token";
    public static final SecretKey KEY = new SecretKeySpec(
            Arrays.copyOf(JWT_SIGN_KEY.getBytes(StandardCharsets.UTF_8), 64), "HmacSHA256");
    // refresh_token cookie 名称
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    // access_token 过期时间 30分钟 (单位: 毫秒)
    public static final Long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L;
    // refresh_token 过期时间 7天 (单位: 毫秒)
    public static final Long REFRESH_TOKEN_EXPIRE_TIME = 60 * 3 * 1000L;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_SCHEMA = "Bearer ";
}
