package com.skillbarter.availability.controller;

import com.skillbarter.availability.dto.AvailabilityResponse;
import com.skillbarter.availability.dto.CreateAvailabilityRequest;
import com.skillbarter.availability.service.AvailabilityOverlapService;
import com.skillbarter.availability.service.AvailabilityService;
import com.skillbarter.common.response.ApiResponse;
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
@RequestMapping("/api/v1/users")
@Tag(name = "Availability", description = "User availability management")
@SecurityRequirement(name = "bearerAuth")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/me/availability")
    @Operation(summary = "Get current user's availability schedule")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getMyAvailability(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(availabilityService.getUserAvailability(userId)));
    }

    @PostMapping("/me/availability")
    @Operation(summary = "Add a new availability time slot")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> createAvailability(
            Authentication authentication,
            @Valid @RequestBody CreateAvailabilityRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        AvailabilityResponse response = availabilityService.createAvailability(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/me/availability/{id}")
    @Operation(summary = "Update an existing availability time slot")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> updateAvailability(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody CreateAvailabilityRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(availabilityService.updateAvailability(userId, id, request)));
    }

    @DeleteMapping("/me/availability/{id}")
    @Operation(summary = "Delete an availability time slot")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        availabilityService.deleteAvailability(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{userId}/availability/overlap")
    @Operation(summary = "Find common availability overlap between current user and target user")
    public ResponseEntity<ApiResponse<List<AvailabilityOverlapService.OverlapSlot>>> getCommonAvailability(
            Authentication authentication,
            @PathVariable UUID userId) {
        UUID meId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(availabilityService.getCommonAvailability(meId, userId)));
    }
}
