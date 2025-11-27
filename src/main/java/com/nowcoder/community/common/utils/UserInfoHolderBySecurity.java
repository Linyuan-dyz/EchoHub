package com.nowcoder.community.common.utils;

import cn.hutool.system.UserInfo;
import com.nowcoder.community.domain.entity.UserDetailsImpl;
import com.nowcoder.community.domain.po.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserInfoHolderBySecurity {

    public static void setUser(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static UserDetails getUserDetails() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal;
    }

    public static User getUser() {
        UserDetails principal = getUserDetails();
        return ((UserDetailsImpl) principal).getUser();
    }

    public static String getRefreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            return "";
        }
        return (String) authentication.getCredentials();
    }
}
