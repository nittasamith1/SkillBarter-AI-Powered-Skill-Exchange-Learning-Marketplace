package com.skillbarter.dispute.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDisputeRequest(
        @NotBlank(message = "Reason is required")
        String reason,

        String description
) {}
