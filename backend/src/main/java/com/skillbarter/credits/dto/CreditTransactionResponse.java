package com.skillbarter.credits.dto;

import com.skillbarter.credits.entity.CreditTransaction;
import com.skillbarter.credits.entity.CreditTransaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditTransactionResponse(
        UUID id,
        UUID userId,
        BigDecimal amount,
        TransactionType type,
        String referenceType,
        UUID referenceId,
        String description,
        Instant createdAt
) {
    public static CreditTransactionResponse from(CreditTransaction t) {
        return new CreditTransactionResponse(
                t.getId(),
                t.getUserId(),
                t.getAmount(),
                t.getType(),
                t.getReferenceType(),
                t.getReferenceId(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}
