package com.skillbarter.reputation.repository;

import com.skillbarter.reputation.entity.TrustScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrustScoreRepository extends JpaRepository<TrustScore, UUID> {

    Optional<TrustScore> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    Optional<TrustScore> findByUserId(UUID userId);
}
