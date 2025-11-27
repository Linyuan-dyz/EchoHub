package com.nowcoder.community.common.config.security.phoneAuth;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PhoneCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public static final String DEFAULT_PHONE_KEY = "phone";
    public static final String DEFAULT_CODE_KEY = "code";
    private static final RequestMatcher DEFAULT_PHONE_REQUEST_MATCHER;
    private String phoneParameter = "phone";
    private String codeParameter = "code";
    private boolean postOnly = true;

    public PhoneCodeAuthenticationFilter() {
        super(DEFAULT_PHONE_REQUEST_MATCHER);
    }

    public PhoneCodeAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_PHONE_REQUEST_MATCHER, authenticationManager);
    }

    /**
     * 实现Authentication的创建和填充，并进行验证
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            PhoneCodeEntity phoneCodeEntity = obtainAuthentication(request);
            PhoneAuthenticationToken authRequest = PhoneAuthenticationToken.unauthenticated(phoneCodeEntity.getPhone(), phoneCodeEntity.getCode());
            this.setDetails(request, authRequest);
            //  将验证流程交给authenticate处理，认证结果交给对应处理器AuthenticationSuccessHandler/AuthenticationFailureHandler
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    @Nullable
    protected PhoneCodeEntity obtainAuthentication(HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(request.getInputStream(), PhoneCodeEntity.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON转换出现问题");
        }
    }


    protected void setDetails(HttpServletRequest request, PhoneAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setPhoneParameter(String phoneParameter) {
        Assert.hasText(phoneParameter, "Phone parameter must not be empty or null");
        this.phoneParameter = phoneParameter;
    }

    public void setCodeParameter(String code) {
        Assert.hasText(code, "Code parameter must not be empty or null");
        this.codeParameter = code;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public String getPhoneParameter() {
        return this.phoneParameter;
    }

    public String getCodeParameter() {
        return this.codeParameter;
    }

    static {
        DEFAULT_PHONE_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/login/phone");
    }
}
