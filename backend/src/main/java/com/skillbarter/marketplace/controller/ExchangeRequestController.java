package com.skillbarter.marketplace.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.marketplace.dto.CreateExchangeRequest;
import com.skillbarter.marketplace.dto.ExchangeRequestResponse;
import com.skillbarter.marketplace.dto.RespondExchangeRequest;
import com.skillbarter.marketplace.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exchange-requests")
@Tag(name = "Exchange Requests", description = "Peer-to-peer skill exchange request endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ExchangeRequestController {

    private final MarketplaceService marketplaceService;

    public ExchangeRequestController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @PostMapping
    @Operation(summary = "Send a new exchange request")
    public ResponseEntity<ApiResponse<ExchangeRequestResponse>> sendRequest(
            Authentication authentication,
            @Valid @RequestBody CreateExchangeRequest request) {
        UUID requesterId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(marketplaceService.sendExchangeRequest(requesterId, request), "Exchange request sent"));
    }

    @GetMapping
    @Operation(summary = "Get all exchange requests involving current user")
    public ResponseEntity<ApiResponse<List<ExchangeRequestResponse>>> getMyRequests(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(marketplaceService.getMyExchangeRequests(userId)));
    }

    @PutMapping("/{id}/respond")
    @Operation(summary = "Respond (accept/reject) to an exchange request")
    public ResponseEntity<ApiResponse<ExchangeRequestResponse>> respondToRequest(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody RespondExchangeRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(marketplaceService.respondToExchangeRequest(userId, id, request), "Response recorded"));
    }
}
