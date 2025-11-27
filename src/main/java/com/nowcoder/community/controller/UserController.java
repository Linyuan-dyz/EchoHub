package com.nowcoder.community.controller;

import com.nowcoder.community.domain.dto.LoginDTO;
import com.nowcoder.community.domain.dto.RegisterDTO;
import com.nowcoder.community.domain.dto.UserDTO;
import com.nowcoder.community.domain.po.User;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.nowcoder.community.common.constant.JWTConstants.REFRESH_TOKEN_COOKIE_NAME;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/code")
    public Result sendCode(@RequestParam String phone) {
        //  TODO:调用第三方api发送验证码
        return userService.sendCode(phone);
    }

    @GetMapping("/refresh")
    public Result refresh(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        return userService.refresh(refreshToken);
    }

    @GetMapping("/me")
    public Result me() {
        return userService.findMe();
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @GetMapping("/logout")
    public Result logout() {
        return userService.logout();
    }

    @PutMapping("/setting")
    public Result update(@RequestBody UserDTO userDTO) {
        return userService.update(userDTO.getHeaderURL(), userDTO.getPassword());
    }

//    @PostMapping("/login")
//    public Result login(@RequestBody LoginDTO loginDTO) {
//        return userService.login(loginDTO);
//    }
}
