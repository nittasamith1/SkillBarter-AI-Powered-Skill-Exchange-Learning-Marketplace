package com.skillbarter.marketplace.repository;

import com.skillbarter.marketplace.entity.ExchangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, UUID> {

    List<ExchangeRequest> findByRequesterIdAndTenantId(UUID requesterId, UUID tenantId);

    List<ExchangeRequest> findByReceiverIdAndTenantId(UUID receiverId, UUID tenantId);

    @Query("SELECT er FROM ExchangeRequest er WHERE er.tenantId = :tenantId AND (er.requesterId = :userId OR er.receiverId = :userId) ORDER BY er.createdAt DESC")
    List<ExchangeRequest> findAllUserRequestsInTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    Optional<ExchangeRequest> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByRequesterIdAndReceiverIdAndOfferedSkillIdAndWantedSkillIdAndStatus(
            UUID requesterId, UUID receiverId, UUID offeredSkillId, UUID wantedSkillId, ExchangeRequest.ExchangeStatus status);
}
