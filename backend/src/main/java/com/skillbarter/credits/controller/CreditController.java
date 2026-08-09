package com.skillbarter.credits.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.credits.dto.CreditTransactionResponse;
import com.skillbarter.credits.dto.WalletResponse;
import com.skillbarter.credits.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credits")
@Tag(name = "Skill Credits", description = "Skill credit wallet and immutable ledger transactions")
@SecurityRequirement(name = "bearerAuth")
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @GetMapping("/wallet")
    @Operation(summary = "Get current user's credit wallet balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(creditService.getWalletResponse(userId)));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get current user's credit transaction history (paginated)")
    public ResponseEntity<ApiResponse<Page<CreditTransactionResponse>>> getTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(creditService.getTransactions(userId, page, size)));
    }
}
