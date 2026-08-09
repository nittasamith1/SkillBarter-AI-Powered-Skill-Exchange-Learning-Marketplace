package com.skillbarter.session.repository;

import com.skillbarter.session.entity.Session;
import com.skillbarter.session.entity.Session.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByTeacherIdOrLearnerIdOrderByScheduledStartDesc(UUID teacherId, UUID learnerId);

    List<Session> findByTenantIdAndTeacherIdOrLearnerIdOrderByScheduledStartDesc(UUID tenantId, UUID teacherId, UUID learnerId);

    Optional<Session> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Check if a user has an active session overlapping with [start, end).
     * Status must be SCHEDULED or IN_PROGRESS.
     */
    @Query("""
        SELECT COUNT(s) FROM Session s
        WHERE (s.teacherId = :userId OR s.learnerId = :userId)
          AND s.status IN ('SCHEDULED', 'IN_PROGRESS')
          AND s.scheduledStart < :end
          AND s.scheduledEnd > :start
    """)
    long countConflictingSessions(
            @Param("userId") UUID userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    List<Session> findByExchangeRequestId(UUID exchangeRequestId);
}
