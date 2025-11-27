package com.nowcoder.community.common.config.security.usernamePasswordAuth;

import com.nowcoder.community.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import java.util.ArrayList;

@Component
public class UsernamePasswordProvider implements AuthenticationProvider {

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                () -> this.messages.getMessage("仅支持UsernamePasswordAuthenticationToken", "当前仅支持UsernamePasswordAuthenticationToken作为验证Token"));
        String username = this.determineUsername(authentication);
        String password = this.determinePassword(authentication);
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
        //  根据username查询到用户数据之后应该要通过相同的加密方式将password加密之后对比密码
        if (userDetails == null || !bCryptPasswordEncoder.matches(password, userDetails.getPassword())) {
            return UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        }
        return UsernamePasswordAuthenticationToken.authenticated(userDetails, null, new ArrayList<>());
    }

    private String determineUsername(Authentication authentication) {
        return authentication.getPrincipal() == null ? "NONE_PROVIDED" : authentication.getName();
    }

    private String determinePassword(Authentication authentication) {
        return authentication.getCredentials() == null ? "NONE_PROVIDED" : authentication.getCredentials().toString();
    }
}
