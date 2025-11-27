package com.nowcoder.community.common.config;

import com.nowcoder.community.common.interceptor.AuthInterceptor;
import com.nowcoder.community.common.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                        .addPathPatterns("/**");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
    }
}
