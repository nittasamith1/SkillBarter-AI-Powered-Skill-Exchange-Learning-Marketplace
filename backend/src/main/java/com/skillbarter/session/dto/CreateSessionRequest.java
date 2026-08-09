package com.skillbarter.session.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateSessionRequest(
        @NotNull(message = "Exchange request ID is required")
        UUID exchangeRequestId,

        @NotNull(message = "Scheduled start time is required")
        Instant scheduledStart,

        @NotNull(message = "Scheduled end time is required")
        Instant scheduledEnd,

        String timezone,
        String meetingLink
) {}
