package com.nowcoder.community.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nowcoder.community.common.constant.RedisConstants;
import com.nowcoder.community.common.utils.EmailUtils;
import com.nowcoder.community.common.utils.JWTUtil;
import com.nowcoder.community.common.utils.RegexUtils;
import com.nowcoder.community.common.utils.UserInfoHolder;
import com.nowcoder.community.domain.dto.LoginDTO;
import com.nowcoder.community.domain.dto.RegisterDTO;
import com.nowcoder.community.domain.po.User;
import com.nowcoder.community.domain.response.Result;
import com.nowcoder.community.domain.vo.UserVO;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.nowcoder.community.common.constant.JWTConstants.REFRESH_TOKEN_COOKIE_NAME;
import static com.nowcoder.community.common.constant.JWTConstants.REFRESH_TOKEN_EXPIRE_TIME;
import static com.nowcoder.community.common.constant.RedisConstants.LOGIN_CODE_KEY;
import static com.nowcoder.community.common.constant.RedisConstants.REFRESH_TOKEN_KEY;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        //  1.验证手机号是否有效
        Boolean isInvalid = RegexUtils.isPhoneInvalid(phone);
        if (isInvalid) {
            return Result.fail("手机号无效");
        }
        //  2.生成六位数随机验证码
        String code = RandomUtil.randomString(6);
        //  3.保存验证码到redis（使用LOGIN_CODE_KEY + phone作为key值储存验证码，设置有效时间）
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code,
                RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //  4.发送验证码
        log.info("验证码为：{}", code);
        return Result.ok(code);
    }

    @Override
    public Result findMe() {
        User user = UserInfoHolder.getUser();
        return Result.ok(BeanUtil.copyProperties(user, UserVO.class));
    }

    @Override
    public Result refresh(String refreshToken) {
        if (Objects.isNull(refreshToken)) {
            log.error("refresh token is null");
            return Result.fail("refresh token is null");
        }
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(REFRESH_TOKEN_KEY + refreshToken);
        if (Objects.isNull(entries) || entries.isEmpty()) {
            log.error("用户信息不存在");
            return Result.fail("令牌已经过期");
        }
        UserVO userVO = BeanUtil.copyProperties(entries, UserVO.class);
        String accessToken = generateAccessTokenAndRefresh(userVO, refreshToken);
        return Result.ok(accessToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (!Objects.isNull(user)) {
            log.error("用户已存在");
            return Result.fail("用户已存在");
        }
        String password = registerDTO.getPassword();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        password = encoder.encode(password);
        String email = registerDTO.getEmail();
//        if (RegexUtils.isEmailInvalid(email)) {
//            log.error("邮箱不正确");
//            return Result.fail("邮箱不正确");
//        }
//        EmailUtils.sendMail(email, "REGISTER", username);
        //  TODO:可以在后面使用消息队列，当用户确认之后发送消息，完成注册程序/或者在此结束，创建另一个接口给前端，完成后续注册操作
        User newUser = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .build();
        userMapper.insert(newUser);
        return Result.ok();
    }

    @Override
    public Result logout() {
        String refreshToken = UserInfoHolder.getRefreshToken();
        stringRedisTemplate.delete(REFRESH_TOKEN_KEY + refreshToken);
        return Result.ok();
    }

    @Override
    public Result update(String headerURL, String password) {
        if (UserInfoHolder.getUser() == null) {
            return Result.fail("登录过期，请重新登录");
        }
        String username = UserInfoHolder.getUser().getUsername();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (Objects.isNull(user)) {
            return Result.fail("用户不存在，请重新登录");
        }
        user.setHeaderUrl(headerURL);
        if (password == null || password.isEmpty()) {
            return Result.fail("密码不可为空");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userMapper.updateById(user);
        return Result.ok();
    }

    @Override
    public Result login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (Objects.isNull(user)) {
            return Result.fail("用户不存在");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            return Result.fail("密码错误");
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        String accessToken = generateAccessTokenAndRefresh(userVO, "");
        return Result.ok();
    }

    /**
     * 生成新accessToken并更新redis的refreshToken
     * @param userVO
     * @return
     */
    private String generateAccessTokenAndRefresh(UserVO userVO, String refreshToken) {
        String accessToken = JWTUtil.generateAccessToken(userVO);
        stringRedisTemplate.delete(LOGIN_CODE_KEY + refreshToken);
        refreshToken = String.valueOf(UUID.randomUUID());
        Map<String, Object> userMap = BeanUtil.beanToMap(userVO, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true).
                        setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(REFRESH_TOKEN_KEY + refreshToken, userMap);
        stringRedisTemplate.opsForHash().expire(REFRESH_TOKEN_KEY + refreshToken,
                Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME), Arrays.asList(userMap.keySet().toArray()));
        return accessToken;
    }

    /**
     * 将refreshToken设置到cookie中
     * @param response
     * @param refreshToken
     */
    private void setRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true); // 防止通过js获取cookie
        cookie.setMaxAge((int) (REFRESH_TOKEN_EXPIRE_TIME / 1000)); // 毫秒转秒
        cookie.setPath("/");
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
}
