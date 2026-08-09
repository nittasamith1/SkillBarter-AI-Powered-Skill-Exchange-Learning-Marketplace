package com.skillbarter.reputation.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.reputation.dto.CreateReviewRequest;
import com.skillbarter.reputation.dto.ReviewResponse;
import com.skillbarter.reputation.dto.TrustScoreResponse;
import com.skillbarter.reputation.entity.TrustScore;
import com.skillbarter.reputation.service.ReviewService;
import com.skillbarter.reputation.service.TrustScoreService;
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
@Tag(name = "Reputation", description = "Session reviews and trust score reputation system")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;
    private final TrustScoreService trustScoreService;

    public ReviewController(ReviewService reviewService, TrustScoreService trustScoreService) {
        this.reviewService = reviewService;
        this.trustScoreService = trustScoreService;
    }

    @PostMapping("/sessions/{id}/review")
    @Operation(summary = "Submit a review for a completed session")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody CreateReviewRequest request) {
        UUID reviewerId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(reviewService.createReview(reviewerId, id, request)));
    }

    @GetMapping("/users/{id}/reviews")
    @Operation(summary = "Get public reviews for a user")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getUserReviews(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getUserReviews(id)));
    }

    @GetMapping("/users/{id}/trust-score")
    @Operation(summary = "Get reputation trust score for a user")
    public ResponseEntity<ApiResponse<TrustScoreResponse>> getTrustScore(@PathVariable UUID id) {
        TrustScore ts = trustScoreService.getTrustScore(id);
        return ResponseEntity.ok(ApiResponse.ok(TrustScoreResponse.from(ts)));
    }
}
