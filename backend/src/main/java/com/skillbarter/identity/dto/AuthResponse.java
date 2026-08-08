package com.skillbarter.identity.dto;

import com.skillbarter.user.dto.UserResponse;

/**
 * Authentication response returned after login or refresh.
 *
 * @param accessToken  short-lived JWT access token (15 minutes)
 * @param refreshToken long-lived refresh token (7 days)
 * @param expiresIn    access token lifetime in seconds
 * @param tokenType    always "Bearer"
 * @param user         safe user summary (no password)
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken,
                                  long expiresInMs, UserResponse user) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                expiresInMs / 1000,   // Convert ms to seconds
                "Bearer",
                user
        );
    }
}
