package com.skillbarter.notification.service;

import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.notification.dto.NotificationResponse;
import com.skillbarter.notification.entity.Notification;
import com.skillbarter.notification.entity.NotificationType;
import com.skillbarter.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(UUID tenantId, UUID userId, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setTenantId(tenantId);
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);

        Notification saved = notificationRepository.save(notification);
        log.info("NOTIFICATION_CREATED requestId=N/A userId={} tenantId={} action=NOTIFICATION_CREATED resourceId={}",
                userId, tenantId, saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, int page, int size) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        return notificationRepository.findByUserIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId)
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        return notificationRepository.countByUserIdAndTenantIdAndReadAtIsNull(userId, tenantId);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.NOTIFICATION_NOT_FOUND, "Notification not found"));

        if (!notification.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException(ErrorCodes.NOTIFICATION_NOT_FOUND, "Notification not found");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        notificationRepository.markAllRead(userId, tenantId, Instant.now());
    }
}
