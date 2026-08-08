package com.skillbarter.identity.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request DTO.
 *
 * @param refreshToken the raw refresh token issued at login
 */
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
