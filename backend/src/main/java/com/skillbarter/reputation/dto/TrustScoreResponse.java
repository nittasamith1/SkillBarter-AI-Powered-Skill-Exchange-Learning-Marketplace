package com.skillbarter.reputation.dto;

import com.skillbarter.reputation.entity.TrustScore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TrustScoreResponse(
        UUID userId,
        BigDecimal trustScore,
        BigDecimal ratingScore,
        BigDecimal completionScore,
        BigDecimal reliabilityScore,
        BigDecimal responseScore,
        BigDecimal cancellationScore,
        Instant calculatedAt
) {
    public static TrustScoreResponse from(TrustScore ts) {
        return new TrustScoreResponse(
                ts.getUserId(),
                ts.getScore(),
                ts.getRatingScore(),
                ts.getCompletionScore(),
                ts.getReliabilityScore(),
                ts.getResponseScore(),
                ts.getCancellationScore(),
                ts.getCalculatedAt()
        );
    }
}
