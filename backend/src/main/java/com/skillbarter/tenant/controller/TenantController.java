package com.skillbarter.tenant.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.tenant.dto.CreateTenantRequest;
import com.skillbarter.tenant.dto.TenantResponse;
import com.skillbarter.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants", description = "Tenant management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new tenant", description = "SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {

        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    @Operation(summary = "Get tenant by ID")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.getTenantById(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> getMyTenant() {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.getMyTenant()));
    }
}
