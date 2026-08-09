package com.skillbarter.credits.repository;

import com.skillbarter.credits.entity.CreditTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {

    Page<CreditTransaction> findByUserIdAndTenantIdOrderByCreatedAtDesc(UUID userId, UUID tenantId, Pageable pageable);

    List<CreditTransaction> findByUserIdAndTenantIdOrderByCreatedAtDesc(UUID userId, UUID tenantId);

    boolean existsByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);
}
