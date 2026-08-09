package com.skillbarter.session.service;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.marketplace.entity.ExchangeRequest;
import com.skillbarter.marketplace.repository.ExchangeRequestRepository;
import com.skillbarter.notification.entity.NotificationType;
import com.skillbarter.notification.service.NotificationService;
import com.skillbarter.session.dto.CreateSessionRequest;
import com.skillbarter.session.dto.SessionResponse;
import com.skillbarter.session.entity.Session;
import com.skillbarter.session.entity.Session.SessionStatus;
import com.skillbarter.session.repository.SessionRepository;
import com.skillbarter.session.validator.SessionStateValidator;
import com.skillbarter.skill.entity.Skill;
import com.skillbarter.skill.repository.SkillRepository;
import com.skillbarter.user.entity.User;
import com.skillbarter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SessionStateValidator stateValidator;
    private final NotificationService notificationService;
    private final com.skillbarter.credits.service.CreditService creditService;

    public SessionService(
            SessionRepository sessionRepository,
            ExchangeRequestRepository exchangeRequestRepository,
            UserRepository userRepository,
            SkillRepository skillRepository,
            SessionStateValidator stateValidator,
            NotificationService notificationService,
            com.skillbarter.credits.service.CreditService creditService) {
        this.sessionRepository = sessionRepository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.stateValidator = stateValidator;
        this.notificationService = notificationService;
        this.creditService = creditService;
    }

    @Transactional
    public SessionResponse createSession(UUID creatorUserId, CreateSessionRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        // 1. Verify exchange request is ACCEPTED
        ExchangeRequest exchangeReq = exchangeRequestRepository.findByIdAndTenantId(request.exchangeRequestId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.EXCHANGE_REQUEST_NOT_ACCEPTED, "Exchange request not found"));

        if (exchangeReq.getStatus() != ExchangeRequest.ExchangeStatus.ACCEPTED) {
            throw new BusinessException(ErrorCodes.EXCHANGE_REQUEST_NOT_ACCEPTED, "Exchange request must be ACCEPTED before scheduling a session");
        }

        // Verify creator is part of the request
        if (!creatorUserId.equals(exchangeReq.getRequesterId()) && !creatorUserId.equals(exchangeReq.getReceiverId())) {
            throw new BusinessException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "You are not authorized to create a session for this exchange request");
        }

        UUID teacherId = exchangeReq.getReceiverId(); // Receiver teaches wanted skill
        UUID learnerId = exchangeReq.getRequesterId();
        UUID skillId = exchangeReq.getWantedSkillId();

        // 2. Validate users exist & are active in tenant
        User teacher = userRepository.findByIdAndTenantId(teacherId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        User learner = userRepository.findByIdAndTenantId(learnerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found"));

        if (teacher.getStatus() != User.UserStatus.ACTIVE || learner.getStatus() != User.UserStatus.ACTIVE) {
            throw new BusinessException("Both users must be active to schedule a session");
        }

        // 3. Slot timing validation
        if (request.scheduledStart().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCodes.SESSION_IN_PAST, "Scheduled start time must be in the future");
        }
        if (!request.scheduledStart().isBefore(request.scheduledEnd())) {
            throw new BusinessException("Scheduled start time must be before end time");
        }
        Duration duration = Duration.between(request.scheduledStart(), request.scheduledEnd());
        if (duration.toMinutes() < 15 || duration.toHours() > 8) {
            throw new BusinessException("Session duration must be between 15 minutes and 8 hours");
        }

        // 4. Server-side Conflict Detection (Double Booking Prevention)
        long teacherConflicts = sessionRepository.countConflictingSessions(teacherId, request.scheduledStart(), request.scheduledEnd());
        if (teacherConflicts > 0) {
            throw new BusinessException(ErrorCodes.SESSION_CONFLICT, "Teacher has a conflicting session schedule");
        }
        long learnerConflicts = sessionRepository.countConflictingSessions(learnerId, request.scheduledStart(), request.scheduledEnd());
        if (learnerConflicts > 0) {
            throw new BusinessException(ErrorCodes.SESSION_CONFLICT, "Learner has a conflicting session schedule");
        }

        Session session = new Session();
        session.setTenantId(tenantId);
        session.setExchangeRequestId(request.exchangeRequestId());
        session.setTeacherId(teacherId);
        session.setLearnerId(learnerId);
        session.setSkillId(skillId);
        session.setScheduledStart(request.scheduledStart());
        session.setScheduledEnd(request.scheduledEnd());
        session.setTimezone(request.timezone() != null ? request.timezone() : "UTC");
        session.setMeetingLink(request.meetingLink() != null ? request.meetingLink() : "https://meet.skillbarter.ai/" + UUID.randomUUID());
        session.setStatus(SessionStatus.SCHEDULED);

        Session saved = sessionRepository.save(session);
        log.info("SESSION_CREATED requestId=N/A userId={} tenantId={} action=SESSION_CREATED resourceId={}",
                creatorUserId, tenantId, saved.getId());

        // Send Notifications
        notificationService.createNotification(tenantId, teacherId, NotificationType.SESSION_SCHEDULED,
                "New Session Scheduled", "You have a session scheduled with " + learner.getFirstName());
        notificationService.createNotification(tenantId, learnerId, NotificationType.SESSION_SCHEDULED,
                "New Session Scheduled", "Your learning session with " + teacher.getFirstName() + " is confirmed");

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getMySessions(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        return sessionRepository.findByTenantIdAndTeacherIdOrLearnerIdOrderByScheduledStartDesc(tenantId, userId, userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(UUID userId, UUID sessionId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Session session = sessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SESSION_NOT_FOUND, "Session not found"));

        if (!session.getTeacherId().equals(userId) && !session.getLearnerId().equals(userId)) {
            throw new BusinessException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "You do not have access to this session");
        }

        return mapToResponse(session);
    }

    @Transactional
    public SessionResponse startSession(UUID userId, UUID sessionId) {
        return updateSessionState(userId, sessionId, SessionStatus.IN_PROGRESS, null);
    }

    @Transactional
    public SessionResponse completeSession(UUID userId, UUID sessionId) {
        return updateSessionState(userId, sessionId, SessionStatus.COMPLETED, null);
    }

    @Transactional
    public SessionResponse cancelSession(UUID userId, UUID sessionId, String reason) {
        return updateSessionState(userId, sessionId, SessionStatus.CANCELLED, reason);
    }

    @Transactional
    public SessionResponse reportNoShow(UUID userId, UUID sessionId, String reason) {
        return updateSessionState(userId, sessionId, SessionStatus.NO_SHOW, reason);
    }

    private SessionResponse updateSessionState(UUID userId, UUID sessionId, SessionStatus targetStatus, String reason) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Session session = sessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SESSION_NOT_FOUND, "Session not found"));

        if (!session.getTeacherId().equals(userId) && !session.getLearnerId().equals(userId)) {
            throw new BusinessException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "You are not a participant in this session");
        }

        stateValidator.validateTransition(session.getStatus(), targetStatus);

        session.setStatus(targetStatus);
        if (reason != null && !reason.isBlank()) {
            session.setCancellationReason(reason);
        }

        Session updated = sessionRepository.save(session);
        log.info("SESSION_STATE_CHANGED status={} sessionId={} userId={} tenantId={}", targetStatus, sessionId, userId, tenantId);

        // Settle credits if session completed
        if (targetStatus == SessionStatus.COMPLETED) {
            creditService.settleSessionCredits(updated);
            notificationService.createNotification(tenantId, session.getTeacherId(), NotificationType.SESSION_COMPLETED,
                    "Session Completed", "Session completed successfully. Credits have been settled.");
            notificationService.createNotification(tenantId, session.getLearnerId(), NotificationType.SESSION_COMPLETED,
                    "Session Completed", "Session completed successfully. Please leave a review!");
        } else if (targetStatus == SessionStatus.CANCELLED) {
            UUID otherId = session.getTeacherId().equals(userId) ? session.getLearnerId() : session.getTeacherId();
            notificationService.createNotification(tenantId, otherId, NotificationType.SESSION_CANCELLED,
                    "Session Cancelled", "Your session was cancelled. Reason: " + (reason != null ? reason : "No reason provided"));
        }

        return mapToResponse(updated);
    }

    private SessionResponse mapToResponse(Session s) {
        String teacherName = userRepository.findById(s.getTeacherId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown Teacher");
        String learnerName = userRepository.findById(s.getLearnerId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown Learner");
        String skillName = skillRepository.findById(s.getSkillId())
                .map(Skill::getName).orElse("Unknown Skill");

        return SessionResponse.from(s, teacherName, learnerName, skillName);
    }
}
