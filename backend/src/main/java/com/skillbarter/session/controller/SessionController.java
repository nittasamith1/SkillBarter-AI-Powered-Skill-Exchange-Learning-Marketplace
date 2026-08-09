package com.skillbarter.session.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.session.dto.CreateSessionRequest;
import com.skillbarter.session.dto.SessionResponse;
import com.skillbarter.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Sessions", description = "Session scheduling and lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @Operation(summary = "Schedule a new session for an accepted exchange request")
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            Authentication authentication,
            @Valid @RequestBody CreateSessionRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(sessionService.createSession(userId, request)));
    }

    @GetMapping
    @Operation(summary = "Get all sessions for current user")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getMySessions(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getMySessions(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session details by ID")
    public ResponseEntity<ApiResponse<SessionResponse>> getSessionById(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getSessionById(userId, id)));
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Start a session (transition SCHEDULED -> IN_PROGRESS)")
    public ResponseEntity<ApiResponse<SessionResponse>> startSession(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(sessionService.startSession(userId, id)));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete a session (transition IN_PROGRESS -> COMPLETED)")
    public ResponseEntity<ApiResponse<SessionResponse>> completeSession(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(sessionService.completeSession(userId, id)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a session")
    public ResponseEntity<ApiResponse<SessionResponse>> cancelSession(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        UUID userId = UUID.fromString(authentication.getName());
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok(sessionService.cancelSession(userId, id, reason)));
    }

    @PatchMapping("/{id}/no-show")
    @Operation(summary = "Report a no-show for a session")
    public ResponseEntity<ApiResponse<SessionResponse>> reportNoShow(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        UUID userId = UUID.fromString(authentication.getName());
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok(sessionService.reportNoShow(userId, id, reason)));
    }
}
