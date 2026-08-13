package com.cheng.game.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 64) String username,
            @NotBlank @Size(min = 6, max = 64) String password,
            String nickname,
            Integer age,
            String sex,
            String location
    ) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record UserView(
            Long id,
            String username,
            String nickname,
            Integer age,
            String sex,
            String location
    ) {
    }

    public record AuthResponse(String token, UserView user) {
    }
}
