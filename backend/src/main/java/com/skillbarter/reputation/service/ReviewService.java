package com.skillbarter.reputation.service;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.notification.entity.NotificationType;
import com.skillbarter.notification.service.NotificationService;
import com.skillbarter.reputation.dto.CreateReviewRequest;
import com.skillbarter.reputation.dto.ReviewResponse;
import com.skillbarter.reputation.entity.Review;
import com.skillbarter.reputation.repository.ReviewRepository;
import com.skillbarter.session.entity.Session;
import com.skillbarter.session.repository.SessionRepository;
import com.skillbarter.user.entity.User;
import com.skillbarter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TrustScoreService trustScoreService;
    private final NotificationService notificationService;

    public ReviewService(
            ReviewRepository reviewRepository,
            SessionRepository sessionRepository,
            UserRepository userRepository,
            TrustScoreService trustScoreService,
            NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.trustScoreService = trustScoreService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReviewResponse createReview(UUID reviewerId, UUID sessionId, CreateReviewRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Session session = sessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SESSION_NOT_FOUND, "Session not found"));

        // 1. Validate session status
        if (session.getStatus() != Session.SessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCodes.REVIEW_NOT_ALLOWED, "Only COMPLETED sessions can be reviewed");
        }

        // 2. Validate reviewer participation
        if (!session.getTeacherId().equals(reviewerId) && !session.getLearnerId().equals(reviewerId)) {
            throw new BusinessException(ErrorCodes.REVIEW_NOT_ALLOWED, "You were not a participant in this session");
        }

        UUID revieweeId = session.getTeacherId().equals(reviewerId) ? session.getLearnerId() : session.getTeacherId();

        // 3. Self review check
        if (reviewerId.equals(revieweeId)) {
            throw new BusinessException(ErrorCodes.REVIEW_NOT_ALLOWED, "You cannot review yourself");
        }

        // 4. Duplicate review check
        if (reviewRepository.existsBySessionIdAndReviewerId(sessionId, reviewerId)) {
            throw new BusinessException(ErrorCodes.REVIEW_ALREADY_EXISTS, "You have already submitted a review for this session");
        }

        Review review = new Review();
        review.setSessionId(sessionId);
        review.setTenantId(tenantId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review saved = reviewRepository.save(review);
        log.info("REVIEW_CREATED requestId=N/A userId={} tenantId={} action=REVIEW_CREATED resourceId={}",
                reviewerId, tenantId, saved.getId());

        // 5. Update Trust Score for reviewee
        trustScoreService.recalculateTrustScore(revieweeId, tenantId);

        // 6. Send Notification
        String reviewerName = userRepository.findById(reviewerId).map(User::getFirstName).orElse("A user");
        notificationService.createNotification(tenantId, revieweeId, NotificationType.REVIEW_RECEIVED,
                "New Review Received", reviewerName + " left a " + request.rating() + "-star review for your session.");

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        return reviewRepository.findByRevieweeIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId)
                .stream().map(this::mapToResponse).toList();
    }

    private ReviewResponse mapToResponse(Review r) {
        String reviewerName = userRepository.findById(r.getReviewerId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");
        String revieweeName = userRepository.findById(r.getRevieweeId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");

        return ReviewResponse.from(r, reviewerName, revieweeName);
    }
}
