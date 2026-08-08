package com.skillbarter.common.security;

import java.util.UUID;

/**
 * Thread-local holder for the authenticated user's tenant identity.
 *
 * <p>The tenant ID is set by {@link JwtAuthenticationFilter} after validating
 * the JWT. Services must always use this context to scope queries — they must
 * NEVER trust a tenant_id supplied by the client.
 *
 * <p>Always call {@link #clear()} in a finally block (done by the filter).
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Utility class — not instantiable
    }

    public static void setCurrentTenant(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
