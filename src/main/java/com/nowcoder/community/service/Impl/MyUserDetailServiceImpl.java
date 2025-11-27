package com.nowcoder.community.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nowcoder.community.domain.entity.UserDetailsImpl;
import com.nowcoder.community.domain.po.User;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailServiceImpl implements MyUserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //  通过数据库方式，根据username查询用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserDetailsImpl(user);
    }

    @Override
    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException(phone);
        }
        return new UserDetailsImpl(user);
    }


}
