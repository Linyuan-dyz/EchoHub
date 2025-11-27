package com.nowcoder.community.common.interceptor;

import com.nowcoder.community.common.utils.UserInfoHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Objects;

@Order(10)
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String URL = request.getRequestURI();
        return true;
    }

    private boolean isPublicURL(String url) {
        String[] publicUrls = {"/login/**", "/user/code", "/user/refresh", "/user/register", "/user/logout"};
        return Arrays.stream(publicUrls).toList().contains(url);
    }

}
