package com.nowcoder.community.common.config.security.phoneAuth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class PhoneAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private Object credentials;

    //  未认证版本构造函数
    public PhoneAuthenticationToken(Object principal, Object credentials) {
        super((Collection)null);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(false);
    }

    //  认证版本构造函数
    public PhoneAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    //  静态方法创建未认证实例
    public static PhoneAuthenticationToken unauthenticated(Object principal, Object credentials) {
        return new PhoneAuthenticationToken(principal, credentials);
    }

    //  静态方法创建认证实例
    public static PhoneAuthenticationToken authenticated(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        return new PhoneAuthenticationToken(principal, credentials, authorities);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
