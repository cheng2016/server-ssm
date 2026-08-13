package com.cheng.game.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cheng.game.app.security.JwtService;
import com.cheng.game.app.web.dto.AuthDtos;
import com.cheng.game.common.error.BusinessException;
import com.cheng.game.common.error.ErrorCode;
import com.cheng.game.persistence.entity.UserEntity;
import com.cheng.game.persistence.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class UserAuthService {

    private static final String ONLINE_KEY = "game:online:players";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    public UserAuthService(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, request.username()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "username already exists");
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname() == null || request.nickname().isBlank()
                ? request.username()
                : request.nickname());
        user.setAge(request.age());
        user.setSex(request.sex());
        user.setLocation(request.location());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userMapper.insert(user);
        return toAuthResponse(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return toAuthResponse(user);
    }

    public AuthDtos.UserView getUser(Long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "user not found");
        }
        return toView(user);
    }

    public JwtService.TokenPayload verifyGameToken(String token) {
        return jwtService.parse(token);
    }

    public void markOnline(Long playerId) {
        redisTemplate.opsForSet().add(ONLINE_KEY, String.valueOf(playerId));
        redisTemplate.expire(ONLINE_KEY, Duration.ofDays(1));
    }

    public void markOffline(Long playerId) {
        redisTemplate.opsForSet().remove(ONLINE_KEY, String.valueOf(playerId));
    }

    public long redisOnlineCount() {
        Long size = redisTemplate.opsForSet().size(ONLINE_KEY);
        return size == null ? 0 : size;
    }

    private AuthDtos.AuthResponse toAuthResponse(UserEntity user) {
        String token = jwtService.createToken(user.getId(), user.getUsername(), user.getNickname());
        return new AuthDtos.AuthResponse(token, toView(user));
    }

    private AuthDtos.UserView toView(UserEntity user) {
        return new AuthDtos.UserView(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAge(),
                user.getSex(),
                user.getLocation());
    }
}
