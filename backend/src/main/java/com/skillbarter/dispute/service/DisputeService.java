package com.skillbarter.dispute.service;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.dispute.dto.CreateDisputeRequest;
import com.skillbarter.dispute.dto.DisputeResponse;
import com.skillbarter.dispute.entity.Dispute;
import com.skillbarter.dispute.entity.Dispute.DisputeStatus;
import com.skillbarter.dispute.repository.DisputeRepository;
import com.skillbarter.notification.entity.NotificationType;
import com.skillbarter.notification.service.NotificationService;
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
public class DisputeService {

    private static final Logger log = LoggerFactory.getLogger(DisputeService.class);

    private final DisputeRepository disputeRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DisputeService(
            DisputeRepository disputeRepository,
            SessionRepository sessionRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.disputeRepository = disputeRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public DisputeResponse createDispute(UUID userId, UUID sessionId, CreateDisputeRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Session session = sessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SESSION_NOT_FOUND, "Session not found"));

        if (!session.getTeacherId().equals(userId) && !session.getLearnerId().equals(userId)) {
            throw new BusinessException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "You are not a participant in this session");
        }

        Dispute dispute = new Dispute();
        dispute.setSessionId(sessionId);
        dispute.setTenantId(tenantId);
        dispute.setRaisedBy(userId);
        dispute.setReason(request.reason());
        dispute.setDescription(request.description());
        dispute.setStatus(DisputeStatus.OPEN);

        Dispute saved = disputeRepository.save(dispute);
        log.info("DISPUTE_CREATED requestId=N/A userId={} tenantId={} action=DISPUTE_CREATED resourceId={}",
                userId, tenantId, saved.getId());

        // Update session status to DISPUTED if completed
        if (session.getStatus() == Session.SessionStatus.COMPLETED) {
            session.setStatus(Session.SessionStatus.DISPUTED);
            sessionRepository.save(session);
        }

        UUID otherParticipant = session.getTeacherId().equals(userId) ? session.getLearnerId() : session.getTeacherId();
        notificationService.createNotification(tenantId, otherParticipant, NotificationType.DISPUTE_CREATED,
                "Dispute Raised", "A dispute was raised for session " + sessionId + ": " + request.reason());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DisputeResponse> getMyDisputes(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        return disputeRepository.findByRaisedByAndTenantIdOrderByCreatedAtDesc(userId, tenantId)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public DisputeResponse getDisputeById(UUID userId, UUID disputeId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Dispute dispute = disputeRepository.findByIdAndTenantId(disputeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.DISPUTE_NOT_FOUND, "Dispute not found"));

        return mapToResponse(dispute);
    }

    private DisputeResponse mapToResponse(Dispute d) {
        String raisedByName = userRepository.findById(d.getRaisedBy())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown User");

        return DisputeResponse.from(d, raisedByName);
    }
}
