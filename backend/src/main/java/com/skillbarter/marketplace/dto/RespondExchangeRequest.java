package com.skillbarter.marketplace.dto;

import com.skillbarter.marketplace.entity.ExchangeRequest;
import jakarta.validation.constraints.NotNull;

public record RespondExchangeRequest(
        @NotNull(message = "Status response is required")
        ExchangeRequest.ExchangeStatus status
) {}
