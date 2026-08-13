package com.cheng.game.app.web;

import com.cheng.game.app.security.JwtService;
import com.cheng.game.app.service.UserAuthService;
import com.cheng.game.app.web.dto.AuthDtos;
import com.cheng.game.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final UserAuthService userAuthService;

    public AuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a player account")
    public ApiResponse<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ApiResponse.ok(userAuthService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and obtain JWT for HTTP / TCP login")
    public ApiResponse<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ApiResponse.ok(userAuthService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Current user profile")
    public ApiResponse<AuthDtos.UserView> me(Authentication authentication) {
        JwtService.TokenPayload payload = (JwtService.TokenPayload) authentication.getPrincipal();
        return ApiResponse.ok(userAuthService.getUser(payload.userId()));
    }
}
