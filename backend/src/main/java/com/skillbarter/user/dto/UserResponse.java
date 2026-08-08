package com.skillbarter.user.dto;

import com.skillbarter.user.entity.User;
import com.skillbarter.tenant.dto.TenantResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Safe user response DTO.
 *
 * <p>Password hash is NEVER included.
 *
 * @param id                user UUID
 * @param email             email address
 * @param firstName         first name
 * @param lastName          last name
 * @param bio               optional biography
 * @param location          optional location
 * @param preferredLanguage preferred language code
 * @param status            account status
 * @param roles             list of role names
 * @param tenant            tenant summary
 * @param createdAt         account creation timestamp
 */
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String bio,
        String location,
        String preferredLanguage,
        String status,
        List<String> roles,
        TenantResponse tenant,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .toList();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getLocation(),
                user.getPreferredLanguage(),
                user.getStatus().name(),
                roleNames,
                TenantResponse.from(user.getTenant()),
                user.getCreatedAt()
        );
    }
}
