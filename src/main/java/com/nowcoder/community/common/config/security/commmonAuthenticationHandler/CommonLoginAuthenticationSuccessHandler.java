package com.nowcoder.community.common.config.security.commmonAuthenticationHandler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.nowcoder.community.common.utils.JWTUtil;
import com.nowcoder.community.common.utils.ResponseUtils;
import com.nowcoder.community.domain.entity.UserDetailsImpl;
import com.nowcoder.community.domain.po.User;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.domain.vo.UserVO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.nowcoder.community.common.constant.JWTConstants.*;
import static com.nowcoder.community.common.constant.RedisConstants.REFRESH_TOKEN_KEY;

@Slf4j
public class CommonLoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 处理成功验证的情况
     * 1.将用户信息储存到redis中并生成refreshToken
     * 2.生成双token并返回到response中
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (!UserDetailsImpl.class.isAssignableFrom(principal.getClass())) {
            log.info("用户信息储存类型出错，不是UserDetailsImpl类型");
            return;
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        User user = userDetails.getUser();
        String refreshToken = generateRefreshToken(user);
        setRefreshTokenToCookie(response, refreshToken);
        String accessToken = generateAccessToken(user);
        setInfoToResponse(user, accessToken, response);
    }

    private void setInfoToResponse(User user, String accessToken, HttpServletResponse response) throws IOException {
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        response.setHeader(AUTHORIZATION_HEADER, accessToken);
        ResponseUtils.result(response, HttpServletResponse.SC_OK, Result.ok(accessToken));
    }

    private String generateAccessToken(User user) {
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return JWTUtil.generateAccessToken(userVO);
    }

    /**
     * 生成随机UUID和REFRESH_TOKEN_KEY作为key，将userVO转换为map以哈希方式储存
     * @param user
     */
    private String generateRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", userVO.getUsername());
        userMap.put("email", userVO.getEmail());
        userMap.put("phone", userVO.getPhone());
        userMap.put("headerUrl", userVO.getHeaderUrl());
        userMap.put("createTime", userVO.getCreateTime() != null ? userVO.getCreateTime().toString() : null);
        stringRedisTemplate.opsForHash().putAll(REFRESH_TOKEN_KEY + refreshToken, userMap);
        stringRedisTemplate.opsForHash().expire(REFRESH_TOKEN_KEY + refreshToken,
                Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME), Arrays.asList(userMap.keySet().toArray()));
        return refreshToken;
    }

    /**
     * 将refreshToken设置到cookie中
     * @param response
     * @param refreshToken
     */
    private void setRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true); // 防止通过js获取cookie
        cookie.setMaxAge((int) (REFRESH_TOKEN_EXPIRE_TIME / 1000)); // 毫秒转秒
        cookie.setPath("/");
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
}
