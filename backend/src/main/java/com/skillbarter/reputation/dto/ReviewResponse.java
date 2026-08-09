package com.skillbarter.reputation.dto;

import com.skillbarter.reputation.entity.Review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID sessionId,
        UUID reviewerId,
        String reviewerName,
        UUID revieweeId,
        String revieweeName,
        int rating,
        String comment,
        Instant createdAt
) {
    public static ReviewResponse from(Review r, String reviewerName, String revieweeName) {
        return new ReviewResponse(
                r.getId(),
                r.getSessionId(),
                r.getReviewerId(),
                reviewerName,
                r.getRevieweeId(),
                revieweeName,
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
