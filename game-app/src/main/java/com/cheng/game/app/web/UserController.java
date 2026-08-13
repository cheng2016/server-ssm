package com.cheng.game.app.web;

import com.cheng.game.app.service.UserAuthService;
import com.cheng.game.app.web.dto.AuthDtos;
import com.cheng.game.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserAuthService userAuthService;

    public UserController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public ApiResponse<AuthDtos.UserView> get(@PathVariable Long id) {
        return ApiResponse.ok(userAuthService.getUser(id));
    }
}
