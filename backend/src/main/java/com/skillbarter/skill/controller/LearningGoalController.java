package com.skillbarter.skill.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.skill.dto.CreateLearningGoalRequest;
import com.skillbarter.skill.dto.LearningGoalResponse;
import com.skillbarter.skill.dto.UpdateLearningGoalRequest;
import com.skillbarter.skill.service.LearningGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/learning-goals")
@Tag(name = "Learning Goals", description = "User learning goals endpoints")
@SecurityRequirement(name = "bearerAuth")
public class LearningGoalController {

    private final LearningGoalService goalService;

    public LearningGoalController(LearningGoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    @Operation(summary = "Get current user's learning goals")
    public ResponseEntity<ApiResponse<List<LearningGoalResponse>>> getMyGoals(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(goalService.getUserGoals(userId)));
    }

    @PostMapping
    @Operation(summary = "Create a new learning goal")
    public ResponseEntity<ApiResponse<LearningGoalResponse>> createGoal(
            Authentication authentication,
            @Valid @RequestBody CreateLearningGoalRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(goalService.createGoal(userId, request), "Learning goal created"));
    }

    @PutMapping("/{goalId}")
    @Operation(summary = "Update a learning goal")
    public ResponseEntity<ApiResponse<LearningGoalResponse>> updateGoal(
            Authentication authentication,
            @PathVariable UUID goalId,
            @RequestBody UpdateLearningGoalRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(goalService.updateGoal(userId, goalId, request), "Learning goal updated"));
    }

    @DeleteMapping("/{goalId}")
    @Operation(summary = "Delete a learning goal")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            Authentication authentication,
            @PathVariable UUID goalId) {
        UUID userId = UUID.fromString(authentication.getName());
        goalService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Learning goal deleted"));
    }
}
