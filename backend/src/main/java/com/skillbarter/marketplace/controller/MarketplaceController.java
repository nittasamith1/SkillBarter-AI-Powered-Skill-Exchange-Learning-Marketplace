package com.skillbarter.marketplace.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.marketplace.dto.PublicUserProfileResponse;
import com.skillbarter.marketplace.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marketplace/users")
@Tag(name = "Marketplace", description = "User discovery and public profiles")
@SecurityRequirement(name = "bearerAuth")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping
    @Operation(summary = "Search users by name or skill")
    public ResponseEntity<ApiResponse<List<PublicUserProfileResponse>>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID skillId) {
        return ResponseEntity.ok(ApiResponse.ok(marketplaceService.searchUsers(query, skillId)));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get public profile of a user by ID")
    public ResponseEntity<ApiResponse<PublicUserProfileResponse>> getPublicProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(marketplaceService.getPublicProfile(userId)));
    }
}
