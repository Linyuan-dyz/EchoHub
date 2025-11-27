package com.nowcoder.community.common.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nowcoder.community.common.utils.JWTUtil;
import com.nowcoder.community.common.utils.UserInfoHolder;
import com.nowcoder.community.domain.po.User;
import com.nowcoder.community.domain.vo.UserVO;
import com.nowcoder.community.mapper.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.nowcoder.community.common.constant.JWTConstants.AUTHORIZATION_HEADER;
import static com.nowcoder.community.common.constant.JWTConstants.REFRESH_TOKEN_COOKIE_NAME;

@Order(1)
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    public TokenInterceptor(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String accessToken = request.getHeader(AUTHORIZATION_HEADER);
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    UserInfoHolder.setRefreshToken(cookie.getValue());
                }
            }
        }

        if (accessToken != null) {
            UserVO userVO = JWTUtil.parseJwtToken(accessToken);
            if (userVO != null) {
                QueryWrapper<User> wrapper = new QueryWrapper<>();
                wrapper.eq("username", userVO.getUsername());
                User user = userMapper.selectOne(wrapper);
                if (user != null) {
                    UserInfoHolder.setUserInfo(user);
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserInfoHolder.removeUser();
    }
}
