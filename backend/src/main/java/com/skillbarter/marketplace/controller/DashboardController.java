package com.skillbarter.marketplace.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.marketplace.dto.DashboardSummaryResponse;
import com.skillbarter.marketplace.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Dashboard summary and recommendations")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final MarketplaceService marketplaceService;

    public DashboardController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping
    @Operation(summary = "Get current user's dashboard summary with recommendations")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboard(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(marketplaceService.getDashboardSummary(userId)));
    }
}
