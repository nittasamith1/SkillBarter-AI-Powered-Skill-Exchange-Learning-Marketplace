package com.skillbarter.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Login request DTO.
 *
 * @param email    user's email
 * @param password user's password (never stored/logged)
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}
