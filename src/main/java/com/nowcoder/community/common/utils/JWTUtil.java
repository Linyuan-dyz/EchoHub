package com.nowcoder.community.common.utils;

import com.nowcoder.community.common.exception.CommonErrorEnum;
import com.nowcoder.community.common.exception.UnauthorizedException;
import com.nowcoder.community.domain.vo.UserVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

import static com.nowcoder.community.common.constant.JWTConstants.*;


/**
 * @author 苍镜月
 * @version 1.0
 * @implNote JWT工具类
 */

@Slf4j
public class JWTUtil {

    // token前缀
    public static final String BEARER = "Bearer ";


    public static String generateAccessToken(UserVO userVO) {
        //  使用email和username构造JWT
        Map<String, Object> userInfoMap = Map.of(USER_EMAIL_KEY, userVO.getEmail(),USER_NAME_KEY, userVO.getUsername());
        String jwtToken = Jwts.builder()
                .addClaims(userInfoMap)
                .setIssuedAt(new Date())
                .setIssuer(ISS)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
        return BEARER + jwtToken;
    }

    /**
     * 解析用户 Token
     *
     * @param jwtToken 用户访问 Token
     * @return 用户信息
     */
    public static UserVO parseJwtToken(String jwtToken) {
        if (StringUtils.hasText(jwtToken)) {
            String actualJwtToken = jwtToken.replace(BEARER, "");
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(KEY)
                        .build()
                        .parseClaimsJws(actualJwtToken)
                        .getBody();
                
                return UserVO.builder()
                        .email(claims.get(USER_EMAIL_KEY, String.class))
                        .username(claims.get(USER_NAME_KEY, String.class))
                        .build();
            } catch (Exception ex) {
                log.warn("JWT Token解析失败，请检查", ex);
                return null;
//                throw new UnauthorizedException(CommonErrorEnum.ACCESS_TOKEN_INVALID);
            }
        }
        return null;
    }

}
