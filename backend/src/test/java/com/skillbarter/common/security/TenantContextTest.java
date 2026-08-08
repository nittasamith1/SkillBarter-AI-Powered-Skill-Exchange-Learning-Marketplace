package com.skillbarter.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @BeforeEach
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("TenantContext holds and clears tenant ID correctly on ThreadLocal")
    void testTenantContextLifecycle() {
        assertNull(TenantContext.getCurrentTenant());

        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);

        assertEquals(tenantId, TenantContext.getCurrentTenant());

        TenantContext.clear();
        assertNull(TenantContext.getCurrentTenant());
    }
}
