package com.skillbarter.session.dto;

import com.skillbarter.session.entity.Session;
import com.skillbarter.session.entity.Session.SessionStatus;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID tenantId,
        UUID exchangeRequestId,
        UUID teacherId,
        String teacherName,
        UUID learnerId,
        String learnerName,
        UUID skillId,
        String skillName,
        Instant scheduledStart,
        Instant scheduledEnd,
        String timezone,
        SessionStatus status,
        String meetingLink,
        String cancellationReason,
        boolean creditsSettled,
        Instant createdAt
) {
    public static SessionResponse from(Session s, String teacherName, String learnerName, String skillName) {
        return new SessionResponse(
                s.getId(),
                s.getTenantId(),
                s.getExchangeRequestId(),
                s.getTeacherId(),
                teacherName,
                s.getLearnerId(),
                learnerName,
                s.getSkillId(),
                skillName,
                s.getScheduledStart(),
                s.getScheduledEnd(),
                s.getTimezone(),
                s.getStatus(),
                s.getMeetingLink(),
                s.getCancellationReason(),
                s.isCreditsSettled(),
                s.getCreatedAt()
        );
    }
}
