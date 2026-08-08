package com.skillbarter.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration request DTO.
 *
 * @param firstName  required first name
 * @param lastName   required last name
 * @param email      valid email address
 * @param password   password (8–72 chars, complexity enforced)
 * @param tenantSlug URL-safe slug identifying the institution to join
 */
public record RegisterRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "Tenant slug is required")
        @Pattern(regexp = "^[a-z0-9-]+$",
                 message = "Tenant slug may only contain lowercase letters, numbers, and hyphens")
        String tenantSlug
) {}
