package com.skillbarter.session.validator;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.session.entity.Session.SessionStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SessionStateValidator {

    private static final Map<SessionStatus, Set<SessionStatus>> ALLOWED_TRANSITIONS = Map.of(
            SessionStatus.SCHEDULED, Set.of(SessionStatus.IN_PROGRESS, SessionStatus.CANCELLED, SessionStatus.NO_SHOW),
            SessionStatus.IN_PROGRESS, Set.of(SessionStatus.COMPLETED, SessionStatus.CANCELLED),
            SessionStatus.COMPLETED, Set.of(SessionStatus.DISPUTED),
            SessionStatus.CANCELLED, Set.of(),
            SessionStatus.NO_SHOW, Set.of(),
            SessionStatus.DISPUTED, Set.of(SessionStatus.COMPLETED, SessionStatus.CANCELLED)
    );

    public void validateTransition(SessionStatus currentStatus, SessionStatus targetStatus) {
        Set<SessionStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new BusinessException(
                    ErrorCodes.INVALID_SESSION_STATE,
                    String.format("Invalid session state transition from %s to %s", currentStatus, targetStatus)
            );
        }
    }
}
