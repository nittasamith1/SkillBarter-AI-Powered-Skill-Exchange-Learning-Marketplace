package com.skillbarter.user.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating the authenticated user's profile.
 *
 * <p>All fields are optional — only non-null values will be applied.
 *
 * @param firstName         optional first name update
 * @param lastName          optional last name update
 * @param bio               optional biography
 * @param location          optional location
 * @param preferredLanguage optional language code (e.g., "en", "hi")
 */
public record UpdateProfileRequest(

        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,

        @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
        String bio,

        @Size(max = 255, message = "Location cannot exceed 255 characters")
        String location,

        @Size(max = 10, message = "Language code cannot exceed 10 characters")
        String preferredLanguage
) {}
