package com.skillbarter.credits.service;

import com.skillbarter.credits.entity.CreditWallet;
import com.skillbarter.credits.repository.CreditTransactionRepository;
import com.skillbarter.credits.repository.CreditWalletRepository;
import com.skillbarter.notification.service.NotificationService;
import com.skillbarter.session.entity.Session;
import com.skillbarter.session.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock private CreditWalletRepository walletRepository;
    @Mock private CreditTransactionRepository transactionRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private CreditService creditService;

    private UUID tenantId;
    private UUID teacherId;
    private UUID learnerId;
    private Session session;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        learnerId = UUID.randomUUID();

        session = new Session();
        session.setId(UUID.randomUUID());
        session.setTenantId(tenantId);
        session.setTeacherId(teacherId);
        session.setLearnerId(learnerId);
        session.setCreditsSettled(false);
    }

    @Test
    @DisplayName("Credit settlement should be idempotent and only execute once per session")
    void testIdempotentSettlement() {
        CreditWallet teacherWallet = new CreditWallet();
        teacherWallet.setUserId(teacherId);
        teacherWallet.setTenantId(tenantId);
        teacherWallet.setBalance(new BigDecimal("10.00"));

        CreditWallet learnerWallet = new CreditWallet();
        learnerWallet.setUserId(learnerId);
        learnerWallet.setTenantId(tenantId);
        learnerWallet.setBalance(new BigDecimal("10.00"));

        when(walletRepository.findByUserIdAndTenantId(teacherId, tenantId)).thenReturn(Optional.of(teacherWallet));
        when(walletRepository.findByUserIdAndTenantId(learnerId, tenantId)).thenReturn(Optional.of(learnerWallet));
        when(transactionRepository.existsByReferenceTypeAndReferenceId("SESSION", session.getId())).thenReturn(false);

        // First call: Settle credits
        boolean result1 = creditService.settleSessionCredits(session);
        assertTrue(result1);
        assertTrue(session.isCreditsSettled());
        assertEquals(new BigDecimal("11.00"), teacherWallet.getBalance());
        assertEquals(new BigDecimal("9.00"), learnerWallet.getBalance());

        // Second call: Already settled -> should return false and not mutate balances again
        lenient().when(transactionRepository.existsByReferenceTypeAndReferenceId("SESSION", session.getId())).thenReturn(true);
        boolean result2 = creditService.settleSessionCredits(session);
        assertFalse(result2);
        assertEquals(new BigDecimal("11.00"), teacherWallet.getBalance());
    }
}
