package com.skillbarter.reputation.repository;

import com.skillbarter.reputation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByRevieweeIdAndTenantIdOrderByCreatedAtDesc(UUID revieweeId, UUID tenantId);

    List<Review> findByRevieweeId(UUID revieweeId);

    boolean existsBySessionIdAndReviewerId(UUID sessionId, UUID reviewerId);
}
