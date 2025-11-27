package com.nowcoder.community.common.exception;

import org.springframework.security.core.AuthenticationException;

public class DoubleTokenAuthenticationException extends AuthenticationException {
    public DoubleTokenAuthenticationException(String message) {
        super(message);
    }
}
