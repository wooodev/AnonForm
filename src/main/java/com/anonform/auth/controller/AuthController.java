package com.anonform.auth.controller;

import com.anonform.auth.dto.LoginRequest;
import com.anonform.auth.dto.RefreshRequest;
import com.anonform.auth.dto.RegisterRequest;
import com.anonform.auth.dto.TokenResponse;
import com.anonform.auth.service.AuthService;
import com.anonform.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Body: { email, password, name }
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /**
     * POST /api/v1/auth/login
     * Body: { email, password }
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /**
     * POST /api/v1/auth/refresh
     * Body: { refreshToken }
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }
}
