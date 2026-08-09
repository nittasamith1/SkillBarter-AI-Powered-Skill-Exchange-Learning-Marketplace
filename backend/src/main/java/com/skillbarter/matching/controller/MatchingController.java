package com.skillbarter.matching.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.matching.dto.MatchCandidateResponse;
import com.skillbarter.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@Tag(name = "Matching", description = "Deterministic baseline matching engine")
@SecurityRequirement(name = "bearerAuth")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping
    @Operation(summary = "Get ranked compatibility match candidates for current user")
    public ResponseEntity<ApiResponse<Page<MatchCandidateResponse>>> getMatches(
            Authentication authentication,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String learningGoal,
            @RequestParam(required = false) String proficiency,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID currentUserId = UUID.fromString(authentication.getName());
        Page<MatchCandidateResponse> matches = matchingService.getMatches(
                currentUserId, skill, learningGoal, proficiency, language, location, page, size);

        return ResponseEntity.ok(ApiResponse.ok(matches));
    }
}
