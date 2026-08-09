package com.skillbarter.credits.service;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.credits.dto.CreditTransactionResponse;
import com.skillbarter.credits.dto.WalletResponse;
import com.skillbarter.credits.entity.CreditTransaction;
import com.skillbarter.credits.entity.CreditTransaction.TransactionType;
import com.skillbarter.credits.entity.CreditWallet;
import com.skillbarter.credits.repository.CreditTransactionRepository;
import com.skillbarter.credits.repository.CreditWalletRepository;
import com.skillbarter.notification.entity.NotificationType;
import com.skillbarter.notification.service.NotificationService;
import com.skillbarter.session.entity.Session;
import com.skillbarter.session.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CreditService {

    private static final Logger log = LoggerFactory.getLogger(CreditService.class);

    private final CreditWalletRepository walletRepository;
    private final CreditTransactionRepository transactionRepository;
    private final SessionRepository sessionRepository;
    private final NotificationService notificationService;

    public CreditService(
            CreditWalletRepository walletRepository,
            CreditTransactionRepository transactionRepository,
            SessionRepository sessionRepository,
            NotificationService notificationService) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.sessionRepository = sessionRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public CreditWallet getOrCreateWallet(UUID userId, UUID tenantId) {
        return walletRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> {
                    CreditWallet wallet = new CreditWallet();
                    wallet.setUserId(userId);
                    wallet.setTenantId(tenantId);
                    wallet.setBalance(new BigDecimal("10.00")); // Initial baseline balance
                    return walletRepository.save(wallet);
                });
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletResponse(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        CreditWallet wallet = getOrCreateWallet(userId, tenantId);
        return WalletResponse.from(wallet);
    }

    @Transactional(readOnly = true)
    public Page<CreditTransactionResponse> getTransactions(UUID userId, int page, int size) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByUserIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId, pageable)
                .map(CreditTransactionResponse::from);
    }

    /**
     * Settle credits for a completed session.
     * IDEMPOTENT & ATOMIC: If already settled, returns safely without creating duplicate transactions.
     */
    @Transactional
    public boolean settleSessionCredits(Session session) {
        UUID tenantId = session.getTenantId();

        // 1. Idempotency Check
        if (session.isCreditsSettled() || transactionRepository.existsByReferenceTypeAndReferenceId("SESSION", session.getId())) {
            log.info("CREDIT_SETTLEMENT_SKIPPED sessionId={} reason=ALREADY_SETTLED", session.getId());
            return false;
        }

        log.info("CREDIT_SETTLEMENT_STARTED sessionId={} teacherId={} learnerId={}",
                session.getId(), session.getTeacherId(), session.getLearnerId());

        CreditWallet teacherWallet = getOrCreateWallet(session.getTeacherId(), tenantId);
        CreditWallet learnerWallet = getOrCreateWallet(session.getLearnerId(), tenantId);

        BigDecimal creditAmount = new BigDecimal("1.00"); // 1 completed session = 1 credit

        // Check if learner has sufficient balance
        if (learnerWallet.getBalance().compareTo(creditAmount) < 0) {
            log.warn("Learner balance low ({}), proceeding with settlement anyway for contract execution", learnerWallet.getBalance());
        }

        // 2. Update Teacher Wallet (+1.00)
        teacherWallet.setBalance(teacherWallet.getBalance().add(creditAmount));
        walletRepository.save(teacherWallet);

        CreditTransaction teacherTx = new CreditTransaction();
        teacherTx.setTenantId(tenantId);
        teacherTx.setUserId(session.getTeacherId());
        teacherTx.setAmount(creditAmount);
        teacherTx.setType(TransactionType.EARN);
        teacherTx.setReferenceType("SESSION");
        teacherTx.setReferenceId(session.getId());
        teacherTx.setDescription("Earned 1.0 Skill Credit from completed teaching session");
        transactionRepository.save(teacherTx);

        // 3. Update Learner Wallet (-1.00)
        BigDecimal newLearnerBalance = learnerWallet.getBalance().subtract(creditAmount);
        learnerWallet.setBalance(newLearnerBalance.max(BigDecimal.ZERO));
        walletRepository.save(learnerWallet);

        CreditTransaction learnerTx = new CreditTransaction();
        learnerTx.setTenantId(tenantId);
        learnerTx.setUserId(session.getLearnerId());
        learnerTx.setAmount(creditAmount.negate());
        learnerTx.setType(TransactionType.SPEND);
        learnerTx.setReferenceType("SESSION");
        learnerTx.setReferenceId(session.getId());
        learnerTx.setDescription("Spent 1.0 Skill Credit on completed learning session");
        transactionRepository.save(learnerTx);

        // 4. Mark session settled
        session.setCreditsSettled(true);
        sessionRepository.save(session);

        log.info("CREDIT_SETTLEMENT_COMPLETED sessionId={} teacherNewBalance={} learnerNewBalance={}",
                session.getId(), teacherWallet.getBalance(), learnerWallet.getBalance());

        // 5. Send Notifications
        notificationService.createNotification(tenantId, session.getTeacherId(), NotificationType.CREDIT_EARNED,
                "Skill Credit Earned", "+1.0 Skill Credit added to your wallet for teaching session");
        notificationService.createNotification(tenantId, session.getLearnerId(), NotificationType.CREDIT_SPENT,
                "Skill Credit Spent", "-1.0 Skill Credit deducted from your wallet for learning session");

        return true;
    }
}
