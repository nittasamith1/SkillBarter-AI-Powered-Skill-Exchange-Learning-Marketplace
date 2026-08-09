package com.skillbarter.dispute.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.dispute.dto.CreateDisputeRequest;
import com.skillbarter.dispute.dto.DisputeResponse;
import com.skillbarter.dispute.service.DisputeService;
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
@RequestMapping("/api/v1")
@Tag(name = "Disputes", description = "Session dispute handling foundation")
@SecurityRequirement(name = "bearerAuth")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping("/sessions/{id}/disputes")
    @Operation(summary = "Raise a dispute for a session")
    public ResponseEntity<ApiResponse<DisputeResponse>> createDispute(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody CreateDisputeRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(disputeService.createDispute(userId, id, request)));
    }

    @GetMapping("/disputes")
    @Operation(summary = "Get all disputes raised by current user")
    public ResponseEntity<ApiResponse<List<DisputeResponse>>> getMyDisputes(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(disputeService.getMyDisputes(userId)));
    }

    @GetMapping("/disputes/{id}")
    @Operation(summary = "Get dispute details by ID")
    public ResponseEntity<ApiResponse<DisputeResponse>> getDisputeById(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(disputeService.getDisputeById(userId, id)));
    }
}
