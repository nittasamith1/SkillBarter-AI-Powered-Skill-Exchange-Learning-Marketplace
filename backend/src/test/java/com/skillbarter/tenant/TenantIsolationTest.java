package com.skillbarter.user.service;

import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.tenant.entity.Tenant;
import com.skillbarter.user.entity.User;
import com.skillbarter.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantIsolationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID tenantAId;
    private UUID tenantBId;
    private UUID userAId;

    @BeforeEach
    void setUp() {
        tenantAId = UUID.randomUUID();
        tenantBId = UUID.randomUUID();
        userAId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("MANDATORY SECURITY TEST: Tenant B authenticated context CANNOT access Tenant A user profile")
    void testTenantBUserCannotAccessTenantAUser() {
        // Given: User A belongs to Tenant A
        Tenant tenantA = new Tenant();
        tenantA.setId(tenantAId);
        tenantA.setName("University A");

        User userA = new User();
        userA.setId(userAId);
        userA.setFirstName("Alice");
        userA.setLastName("Smith");
        userA.setTenant(tenantA);

        // Mock repository returning empty when queried with userAId and tenantBId
        when(userRepository.findByIdAndTenantId(eq(userAId), eq(tenantBId)))
                .thenReturn(Optional.empty());

        // When: TenantContext is set to Tenant B (authenticated User B)
        TenantContext.setCurrentTenant(tenantBId);

        // Then: Attempting to fetch User A throws ResourceNotFoundException (preventing data leak)
        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser(userAId));

        verify(userRepository).findByIdAndTenantId(userAId, tenantBId);
    }
}
