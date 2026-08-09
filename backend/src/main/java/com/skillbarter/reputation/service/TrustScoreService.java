package com.skillbarter.reputation.service;

import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.reputation.entity.Review;
import com.skillbarter.reputation.entity.TrustScore;
import com.skillbarter.reputation.repository.ReviewRepository;
import com.skillbarter.reputation.repository.TrustScoreRepository;
import com.skillbarter.session.entity.Session;
import com.skillbarter.session.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TrustScoreService {

    private static final Logger log = LoggerFactory.getLogger(TrustScoreService.class);

    private final TrustScoreRepository trustScoreRepository;
    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;
    private final TrustScoreConfig config;

    public TrustScoreService(
            TrustScoreRepository trustScoreRepository,
            ReviewRepository reviewRepository,
            SessionRepository sessionRepository,
            TrustScoreConfig config) {
        this.trustScoreRepository = trustScoreRepository;
        this.reviewRepository = reviewRepository;
        this.sessionRepository = sessionRepository;
        this.config = config;
    }

    @Transactional
    public TrustScore recalculateTrustScore(UUID userId, UUID tenantId) {
        TrustScore ts = trustScoreRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> {
                    TrustScore score = new TrustScore();
                    score.setUserId(userId);
                    score.setTenantId(tenantId);
                    return score;
                });

        // 1. Calculate Average Rating (40%)
        List<Review> reviews = reviewRepository.findByRevieweeId(userId);
        double avgRatingScore = 100.0;
        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
            avgRatingScore = (avgRating / 5.0) * 100.0;
        }

        // 2. Calculate Session Stats (Completion 20%, Reliability 20%, Cancellation 10%)
        List<Session> sessions = sessionRepository.findByTeacherIdOrLearnerIdOrderByScheduledStartDesc(userId, userId);
        double completionScore = 100.0;
        double reliabilityScore = 100.0;
        double cancellationScore = 100.0;

        if (!sessions.isEmpty()) {
            long total = sessions.size();
            long completed = sessions.stream().filter(s -> s.getStatus() == Session.SessionStatus.COMPLETED).count();
            long noShows = sessions.stream().filter(s -> s.getStatus() == Session.SessionStatus.NO_SHOW).count();
            long cancelled = sessions.stream().filter(s -> s.getStatus() == Session.SessionStatus.CANCELLED).count();

            completionScore = ((double) completed / total) * 100.0;
            reliabilityScore = (((double) (total - noShows)) / total) * 100.0;
            cancellationScore = (((double) (total - cancelled)) / total) * 100.0;
        }

        double responseScore = 95.0; // Baseline response score

        // 3. Weighted Total
        double totalScore = (avgRatingScore * config.getRating()) +
                           (completionScore * config.getCompletion()) +
                           (reliabilityScore * config.getReliability()) +
                           (responseScore * config.getResponse()) +
                           (cancellationScore * config.getCancellation());

        ts.setRatingScore(BigDecimal.valueOf(avgRatingScore).setScale(2, RoundingMode.HALF_UP));
        ts.setCompletionScore(BigDecimal.valueOf(completionScore).setScale(2, RoundingMode.HALF_UP));
        ts.setReliabilityScore(BigDecimal.valueOf(reliabilityScore).setScale(2, RoundingMode.HALF_UP));
        ts.setResponseScore(BigDecimal.valueOf(responseScore).setScale(2, RoundingMode.HALF_UP));
        ts.setCancellationScore(BigDecimal.valueOf(cancellationScore).setScale(2, RoundingMode.HALF_UP));
        ts.setScore(BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP));
        ts.setCalculatedAt(Instant.now());

        TrustScore saved = trustScoreRepository.save(ts);
        log.info("TRUST_SCORE_UPDATED userId={} tenantId={} newScore={}", userId, tenantId, saved.getScore());
        return saved;
    }

    @Transactional(readOnly = true)
    public TrustScore getTrustScore(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        return trustScoreRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> recalculateTrustScore(userId, tenantId));
    }
}
