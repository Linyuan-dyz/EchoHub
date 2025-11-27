package com.nowcoder.community.common.config.security.doubleTokenAuth;

import cn.hutool.core.text.AntPathMatcher;
import com.nowcoder.community.common.config.security.phoneAuth.PhoneAuthenticationToken;
import com.nowcoder.community.common.exception.CommonErrorEnum;
import com.nowcoder.community.common.exception.UnauthorizedException;
import com.nowcoder.community.common.utils.JWTUtil;
import com.nowcoder.community.domain.vo.UserVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.*;

import java.util.Objects;

import static com.nowcoder.community.common.constant.JWTConstants.*;

@Slf4j
public class DoubleTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final RequestMatcher DEFAULT_REQUEST_MATCHER;

    public DoubleTokenAuthenticationFilter() {
        super(DEFAULT_REQUEST_MATCHER);
    }

    public DoubleTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_REQUEST_MATCHER, authenticationManager);
    }

    /**
     * 从请求中获取双token，检验操作交给authenticate方法处理
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 从请求头获取 token： Authorization: Bearer <token>
        String accessToken = request.getHeader(AUTHORIZATION_HEADER);
        // 如果为空或不是 Bearer 令牌
        if (Objects.isNull(accessToken) || !accessToken.startsWith(AUTHORIZATION_SCHEMA)) {
            log.error("令牌不存在或没有以'Bearer'为开头");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        log.debug("accessToken:{}", accessToken);

        String refreshToken = null;
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        DoubleTokenAuthenticationToken authRequest = new DoubleTokenAuthenticationToken(accessToken, refreshToken);
        this.setDetails(request, authRequest);
        //  将验证流程交给authenticate处理，认证结果交给对应处理器AuthenticationSuccessHandler/AuthenticationFailureHandler
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    protected void setDetails(HttpServletRequest request, DoubleTokenAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    static {
        DEFAULT_REQUEST_MATCHER = new NegatedRequestMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/login/**"),
                new AntPathRequestMatcher("/user/code"),
                new AntPathRequestMatcher("/user/refresh"),
                new AntPathRequestMatcher("/user/register"),
                new AntPathRequestMatcher("/user/logout")
        ));
    }
}
