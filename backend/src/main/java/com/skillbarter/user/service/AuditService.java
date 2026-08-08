package com.skillbarter.user.service;

import com.skillbarter.user.entity.AuditLog;
import com.skillbarter.user.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void log(UUID tenantId, UUID userId, String action,
                    String resourceType, String resourceId,
                    String ipAddress) {

        try {
            AuditLog entry = new AuditLog();
            entry.setTenantId(tenantId);
            entry.setUserId(userId);
            entry.setAction(action);
            entry.setResourceType(resourceType);
            entry.setResourceId(resourceId);
            entry.setIpAddress(ipAddress);

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: action={} userId={}", action, userId, e);
        }
    }

    @Async
    public void log(UUID tenantId, UUID userId, String action, String ipAddress) {
        log(tenantId, userId, action, null, null, ipAddress);
    }
}
