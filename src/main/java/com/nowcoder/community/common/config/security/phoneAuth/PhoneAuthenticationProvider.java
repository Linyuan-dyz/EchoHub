package com.nowcoder.community.common.config.security.phoneAuth;

import com.nowcoder.community.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;

import static com.nowcoder.community.common.constant.RedisConstants.LOGIN_CODE_KEY;

@Component
public class PhoneAuthenticationProvider implements AuthenticationProvider {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    /**
     * 表明如何实现手机号+验证码的验证
     * 验证后根据结果是否成功交给对应处理器进行处理。
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(PhoneAuthenticationToken.class, authentication,
                () -> this.messages.getMessage("仅支持PhoneAuthenticationProvider", "当前仅支持PhoneAuthenticationToken作为验证Token"));
        String phone = this.determinePhone(authentication);
        String code = this.determineCode(authentication);
        //  redis中获取手机号对应的验证码
        System.out.println(LOGIN_CODE_KEY + phone);
        String storeCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        //  如果验证码不存在，则认证失败
        if (storeCode == null || !storeCode.equals(code)) {
            return PhoneAuthenticationToken.unauthenticated(phone, null);
        }
        //  Controller层不再需要创建一个api用来处理手机号登录了
        return successAuthentication(authentication);
    }

    private String determinePhone(Authentication authentication) {
        return authentication.getPrincipal() == null ? "NONE_PROVIDED" : authentication.getName();
    }

    private String determineCode(Authentication authentication) {
        return authentication.getCredentials() == null ? "NONE_PROVIDED" : authentication.getCredentials().toString();
    }

    /**
     * TODO:处理手机号还没有注册的情况
     * 查询成功认证的手机号对应用户并封装入authentication中
     * @param authentication
     * @return
     */
    private Authentication successAuthentication(Authentication authentication) {
        String phone = this.determinePhone(authentication);
        UserDetails userDetails = myUserDetailsService.loadUserByPhone(phone);
        if (userDetails == null) {
            //  TODO
            throw new RuntimeException();
        }
        return PhoneAuthenticationToken.authenticated(userDetails, null, new ArrayList<>());
    }

    //  表明支持什么类来认证
    @Override
    public boolean supports(Class<?> authentication) {
        return PhoneAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
