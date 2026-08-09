package com.skillbarter.dispute.dto;

import com.skillbarter.dispute.entity.Dispute;
import com.skillbarter.dispute.entity.Dispute.DisputeStatus;

import java.time.Instant;
import java.util.UUID;

public record DisputeResponse(
        UUID id,
        UUID sessionId,
        UUID raisedBy,
        String raisedByName,
        String reason,
        String description,
        DisputeStatus status,
        String resolution,
        Instant createdAt,
        Instant resolvedAt
) {
    public static DisputeResponse from(Dispute d, String raisedByName) {
        return new DisputeResponse(
                d.getId(),
                d.getSessionId(),
                d.getRaisedBy(),
                raisedByName,
                d.getReason(),
                d.getDescription(),
                d.getStatus(),
                d.getResolution(),
                d.getCreatedAt(),
                d.getResolvedAt()
        );
    }
}
