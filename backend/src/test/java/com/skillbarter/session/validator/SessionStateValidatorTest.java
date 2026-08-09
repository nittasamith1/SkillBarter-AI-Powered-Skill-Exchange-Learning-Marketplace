package com.skillbarter.session.validator;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.session.entity.Session.SessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionStateValidatorTest {

    private final SessionStateValidator validator = new SessionStateValidator();

    @Test
    @DisplayName("Valid transition: SCHEDULED -> IN_PROGRESS -> COMPLETED")
    void testValidTransitions() {
        assertDoesNotThrow(() -> validator.validateTransition(SessionStatus.SCHEDULED, SessionStatus.IN_PROGRESS));
        assertDoesNotThrow(() -> validator.validateTransition(SessionStatus.IN_PROGRESS, SessionStatus.COMPLETED));
        assertDoesNotThrow(() -> validator.validateTransition(SessionStatus.SCHEDULED, SessionStatus.CANCELLED));
    }

    @Test
    @DisplayName("Invalid transition: COMPLETED -> IN_PROGRESS must fail with INVALID_SESSION_STATE")
    void testInvalidTransitionThrowsException() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                validator.validateTransition(SessionStatus.COMPLETED, SessionStatus.IN_PROGRESS));
        assertEquals("INVALID_SESSION_STATE", ex.getCode());
    }
}
