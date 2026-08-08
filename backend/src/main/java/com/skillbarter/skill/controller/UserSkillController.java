package com.skillbarter.skill.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.skill.dto.AddUserSkillRequest;
import com.skillbarter.skill.dto.UpdateUserSkillRequest;
import com.skillbarter.skill.dto.UserSkillResponse;
import com.skillbarter.skill.service.UserSkillService;
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
@RequestMapping("/api/v1/users")
@Tag(name = "User Skills", description = "User profile skill management")
@SecurityRequirement(name = "bearerAuth")
public class UserSkillController {

    private final UserSkillService userSkillService;

    public UserSkillController(UserSkillService userSkillService) {
        this.userSkillService = userSkillService;
    }

    @GetMapping("/me/skills")
    @Operation(summary = "Get current user's profile skills")
    public ResponseEntity<ApiResponse<List<UserSkillResponse>>> getMySkills(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(userSkillService.getUserSkills(userId)));
    }

    @PostMapping("/me/skills")
    @Operation(summary = "Add a skill to current user's profile")
    public ResponseEntity<ApiResponse<UserSkillResponse>> addSkillToMe(
            Authentication authentication,
            @Valid @RequestBody AddUserSkillRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(userSkillService.addUserSkill(userId, request), "Skill added to profile"));
    }

    @PutMapping("/me/skills/{skillId}")
    @Operation(summary = "Update a skill on current user's profile")
    public ResponseEntity<ApiResponse<UserSkillResponse>> updateMySkill(
            Authentication authentication,
            @PathVariable UUID skillId,
            @RequestBody UpdateUserSkillRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(userSkillService.updateUserSkill(userId, skillId, request), "Skill updated"));
    }

    @DeleteMapping("/me/skills/{skillId}")
    @Operation(summary = "Remove a skill from current user's profile")
    public ResponseEntity<ApiResponse<Void>> removeMySkill(
            Authentication authentication,
            @PathVariable UUID skillId) {
        UUID userId = UUID.fromString(authentication.getName());
        userSkillService.removeUserSkill(userId, skillId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Skill removed from profile"));
    }

    @GetMapping("/{userId}/skills")
    @Operation(summary = "Get public skills of any user")
    public ResponseEntity<ApiResponse<List<UserSkillResponse>>> getUserSkills(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(userSkillService.getUserSkills(userId)));
    }
}
