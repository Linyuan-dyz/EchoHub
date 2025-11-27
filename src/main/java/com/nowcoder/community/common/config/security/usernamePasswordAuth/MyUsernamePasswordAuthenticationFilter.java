package com.nowcoder.community.common.config.security.usernamePasswordAuth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class MyUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private boolean postOnly = true;

    public MyUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            UsernamePasswordEntity usernamePasswordEntity = obtainAuthentication(request);
            UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(usernamePasswordEntity.getUsername(), usernamePasswordEntity.getPassword());
            this.setDetails(request, authRequest);
            //  将验证流程交给authenticate处理，认证结果交给对应处理器AuthenticationSuccessHandler/AuthenticationFailureHandler
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    @Nullable
    protected UsernamePasswordEntity obtainAuthentication(HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(request.getInputStream(), UsernamePasswordEntity.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON转换出现问题");
        }
    }

    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

}