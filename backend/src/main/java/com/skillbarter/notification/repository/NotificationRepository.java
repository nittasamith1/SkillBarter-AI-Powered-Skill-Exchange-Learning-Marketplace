package com.skillbarter.notification.repository;

import com.skillbarter.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdAndTenantIdOrderByCreatedAtDesc(UUID userId, UUID tenantId, Pageable pageable);

    List<Notification> findByUserIdAndTenantIdOrderByCreatedAtDesc(UUID userId, UUID tenantId);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndTenantIdAndReadAtIsNull(UUID userId, UUID tenantId);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.userId = :userId AND n.tenantId = :tenantId AND n.readAt IS NULL")
    int markAllRead(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId, @Param("now") Instant now);
}
