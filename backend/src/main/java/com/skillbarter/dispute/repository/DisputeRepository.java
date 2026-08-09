package com.skillbarter.dispute.repository;

import com.skillbarter.dispute.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {

    List<Dispute> findByRaisedByAndTenantIdOrderByCreatedAtDesc(UUID raisedBy, UUID tenantId);

    List<Dispute> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<Dispute> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Dispute> findBySessionId(UUID sessionId);
}
