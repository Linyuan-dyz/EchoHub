package com.nowcoder.community.common.config.security;

import com.nowcoder.community.common.config.security.doubleTokenAuth.DoubleTokenAuthenticationFailureHandler;
import com.nowcoder.community.common.config.security.doubleTokenAuth.DoubleTokenAuthenticationFilter;
import com.nowcoder.community.common.config.security.doubleTokenAuth.DoubleTokenAuthenticationProvider;
import com.nowcoder.community.common.config.security.doubleTokenAuth.DoubleTokenAuthenticationSuccessHandler;
import com.nowcoder.community.common.config.security.usernamePasswordAuth.MyUsernamePasswordAuthenticationFilter;
import com.nowcoder.community.common.config.security.usernamePasswordAuth.UsernameAuthenticationFailureHandler;
import com.nowcoder.community.common.config.security.usernamePasswordAuth.UsernameAuthenticationSuccessHandler;
import com.nowcoder.community.common.config.security.usernamePasswordAuth.UsernamePasswordProvider;
import com.nowcoder.community.common.config.security.phoneAuth.PhoneAuthenticationFailureHandler;
import com.nowcoder.community.common.config.security.phoneAuth.PhoneAuthenticationProvider;
import com.nowcoder.community.common.config.security.phoneAuth.PhoneAuthenticationSuccessHandler;
import com.nowcoder.community.common.config.security.phoneAuth.PhoneCodeAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;


/**
 * spring security配置类
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final PhoneAuthenticationFailureHandler phoneAuthenticationFailureHandler;

    private final PhoneAuthenticationSuccessHandler phoneAuthenticationSuccessHandler;

    private final PhoneAuthenticationProvider phoneAuthenticationProvider;

    private final UsernameAuthenticationSuccessHandler usernameAuthenticationSuccessHandler;

    private final UsernameAuthenticationFailureHandler usernameAuthenticationFailureHandler;

    private final UsernamePasswordProvider usernamePasswordProvider;

    private final DoubleTokenAuthenticationFailureHandler doubleTokenAuthenticationFailureHandler;

    private final DoubleTokenAuthenticationSuccessHandler doubleTokenAuthenticationSuccessHandler;

    private final DoubleTokenAuthenticationProvider doubleTokenAuthenticationProvider;

    /**
     * Filter一般不需要添加为bean，否则可能被两次调用：filterChain一次，spring bean一次
     * @return
     */
    public PhoneCodeAuthenticationFilter phoneCodeAuthenticationFilter() {
        PhoneCodeAuthenticationFilter phoneCodeAuthenticationFilter = new PhoneCodeAuthenticationFilter(new ProviderManager(phoneAuthenticationProvider));
        phoneCodeAuthenticationFilter.setAuthenticationSuccessHandler(phoneAuthenticationSuccessHandler);
        phoneCodeAuthenticationFilter.setAuthenticationFailureHandler(phoneAuthenticationFailureHandler);
        return phoneCodeAuthenticationFilter;
    }

    public MyUsernamePasswordAuthenticationFilter myUsernamePasswordAuthenticationFilter() {
        MyUsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter = new MyUsernamePasswordAuthenticationFilter(new ProviderManager(usernamePasswordProvider));
        usernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(usernameAuthenticationSuccessHandler);
        usernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(usernameAuthenticationFailureHandler);
        return usernamePasswordAuthenticationFilter;
    }

    @Order(1)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatchers(matcher -> {
                    matcher.requestMatchers("/login/**");
                })
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login/phone", "/login").authenticated()
                )
                .addFilterBefore(phoneCodeAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(myUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Order(10)
//    @Bean
//    public SecurityFilterChain doubleTokenFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/login/**", "/user/code", "/user/refresh", "/user/register", "/user/logout").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(doubleTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(exceptions -> exceptions
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                            response.setContentType("application/json;charset=UTF-8");
//                            response.getWriter().write("{\"code\":401,\"message\":\"未授权访问\"}");
//                        })
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                            response.setContentType("application/json;charset=UTF-8");
//                            response.getWriter().write("{\"code\":403,\"message\":\"访问被拒绝\"}");
//                        })
//                );
//
//        return http.build();
//    }

    public DoubleTokenAuthenticationFilter doubleTokenAuthenticationFilter() {
        DoubleTokenAuthenticationFilter doubleTokenAuthenticationFilter = new DoubleTokenAuthenticationFilter(new ProviderManager(doubleTokenAuthenticationProvider));
        doubleTokenAuthenticationFilter.setAuthenticationSuccessHandler(doubleTokenAuthenticationSuccessHandler);
        doubleTokenAuthenticationFilter.setAuthenticationFailureHandler(doubleTokenAuthenticationFailureHandler);
        return doubleTokenAuthenticationFilter;
    }


}
