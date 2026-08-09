package com.skillbarter.credits.dto;

import com.skillbarter.credits.entity.CreditWallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        UUID userId,
        BigDecimal balance,
        Instant updatedAt
) {
    public static WalletResponse from(CreditWallet w) {
        return new WalletResponse(
                w.getId(),
                w.getUserId(),
                w.getBalance(),
                w.getUpdatedAt()
        );
    }
}
