package com.skillbarter.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new tenant.
 *
 * @param name human-readable tenant name
 * @param slug URL-safe unique identifier
 */
public record CreateTenantRequest(

        @NotBlank(message = "Tenant name is required")
        @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(min = 2, max = 100, message = "Slug must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-z0-9-]+$",
                 message = "Slug may only contain lowercase letters, numbers, and hyphens")
        String slug
) {}
