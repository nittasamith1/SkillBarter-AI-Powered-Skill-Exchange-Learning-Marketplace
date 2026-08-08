package com.skillbarter.tenant.dto;

import com.skillbarter.tenant.entity.Tenant;

import java.time.Instant;
import java.util.UUID;

/**
 * Safe tenant response DTO — no internal fields exposed.
 *
 * @param id        tenant UUID
 * @param name      tenant name
 * @param slug      tenant slug
 * @param status    tenant status
 * @param createdAt creation timestamp
 */
public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String status,
        Instant createdAt
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getStatus().name(),
                tenant.getCreatedAt()
        );
    }
}
