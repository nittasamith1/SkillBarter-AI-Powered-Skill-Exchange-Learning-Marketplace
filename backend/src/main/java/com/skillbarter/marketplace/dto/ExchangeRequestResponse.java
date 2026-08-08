package com.skillbarter.marketplace.dto;

import com.skillbarter.marketplace.entity.ExchangeRequest;

import java.time.Instant;
import java.util.UUID;

public record ExchangeRequestResponse(
        UUID id,
        UUID requesterId,
        String requesterName,
        UUID receiverId,
        String receiverName,
        UUID offeredSkillId,
        String offeredSkillName,
        UUID wantedSkillId,
        String wantedSkillName,
        String message,
        ExchangeRequest.ExchangeStatus status,
        Instant createdAt
) {}
