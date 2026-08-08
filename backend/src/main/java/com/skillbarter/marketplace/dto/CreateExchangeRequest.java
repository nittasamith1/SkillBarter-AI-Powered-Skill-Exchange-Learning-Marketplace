package com.skillbarter.marketplace.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateExchangeRequest(
        @NotNull(message = "Receiver ID is required")
        UUID receiverId,

        @NotNull(message = "Offered skill ID is required")
        UUID offeredSkillId,

        @NotNull(message = "Wanted skill ID is required")
        UUID wantedSkillId,

        String message
) {}
