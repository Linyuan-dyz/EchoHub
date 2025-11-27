package com.nowcoder.community.common.config.security.doubleTokenAuth;

import com.nowcoder.community.common.utils.UserInfoHolder;
import com.nowcoder.community.common.utils.UserInfoHolderBySecurity;
import com.nowcoder.community.domain.entity.UserDetailsImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class DoubleTokenAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (!UserDetailsImpl.class.isAssignableFrom(principal.getClass())) {
            log.info("用户信息储存类型出错，不是UserDetailsImpl类型");
            return;
        }
        UserInfoHolderBySecurity.setUser(authentication);
    }
}
