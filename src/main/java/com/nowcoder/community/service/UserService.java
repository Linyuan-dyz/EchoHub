package com.nowcoder.community.service;


import com.nowcoder.community.domain.dto.LoginDTO;
import com.nowcoder.community.domain.dto.RegisterDTO;
import com.nowcoder.community.domain.response.Result;

public interface UserService {

    Result sendCode(String phone);

    Result findMe();

    Result refresh(String refreshToken);

    Result register(RegisterDTO registerDTO);

    Result logout();

    Result update(String headerURL, String password);

    Result login(LoginDTO loginDTO);
}
