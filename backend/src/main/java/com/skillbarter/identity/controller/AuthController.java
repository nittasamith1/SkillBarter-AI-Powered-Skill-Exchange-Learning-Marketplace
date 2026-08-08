package com.skillbarter.identity.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.identity.dto.AuthResponse;
import com.skillbarter.identity.dto.LoginRequest;
import com.skillbarter.identity.dto.RefreshTokenRequest;
import com.skillbarter.identity.dto.RegisterRequest;
import com.skillbarter.identity.service.AuthService;
import com.skillbarter.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication & registration endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Public endpoint to register a new user under a tenant slug")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        UserResponse user = authService.register(request, getClientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(user, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Public endpoint to authenticate with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse authResponse = authService.login(request, getClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.ok(authResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token for a new access token and refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse authResponse = authService.refreshToken(request, getClientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.ok(authResponse, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates the refresh token and terminates session")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String token = request != null ? request.refreshToken() : null;
        String userIdStr = authentication != null ? authentication.getName() : null;
        authService.logout(token, userIdStr, getClientIp(httpRequest));

        return ResponseEntity.ok(ApiResponse.ok(null, "Logged out successfully"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
