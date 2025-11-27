package com.nowcoder.community.common.config.security.doubleTokenAuth;

import com.nowcoder.community.common.exception.CommonErrorEnum;
import com.nowcoder.community.common.exception.DoubleTokenAuthenticationException;
import com.nowcoder.community.common.exception.UnauthorizedException;
import com.nowcoder.community.common.utils.JWTUtil;
import com.nowcoder.community.domain.vo.UserVO;
import com.nowcoder.community.service.MyUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Component
@Slf4j
public class DoubleTokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    /**
     * 双token中认证模块只需要考虑校验accessToken，刷新逻辑交给调用刷新接口来做，如果发现accessToken过期，前端会调用刷新接口进行处理
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String accessToken = determineAccessToken(authentication);
        String refreshToken = determineRefreshToken(authentication);
        if (accessToken.isEmpty()) {
            throw new DoubleTokenAuthenticationException("不存在accessToken");
        }
        UserVO userVO = null;
        try {
            userVO = JWTUtil.parseJwtToken(accessToken);
        } catch (UnauthorizedException e) {
            throw new DoubleTokenAuthenticationException("accessToken解析失败");
        }
        // 解析失败
        if (Objects.isNull(userVO)) {
            log.error("令牌无效");
            throw new DoubleTokenAuthenticationException("accessToken令牌无效");
        }
        log.debug("username:{}, phone:{}", userVO.getUsername(), userVO.getPhone());
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(userVO.getUsername());
        if (userDetails == null) {
            log.error("不存在对应的用户");
            throw new DoubleTokenAuthenticationException("不存在对应的用户");
        }
        return DoubleTokenAuthenticationToken.authenticated(userDetails, refreshToken, new ArrayList<>());
    }

    private String determineAccessToken(Authentication authentication) {
        return authentication.getPrincipal() == null ? "" : authentication.getPrincipal().toString();
    }

    private String determineRefreshToken(Authentication authentication) {
        return authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return DoubleTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
