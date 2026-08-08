package com.skillbarter.skill.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.skill.dto.CreateSkillRequest;
import com.skillbarter.skill.dto.ExploreSkillResponse;
import com.skillbarter.skill.dto.SkillResponse;
import com.skillbarter.skill.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/skills")
@Tag(name = "Skills", description = "Skill catalog and search endpoints")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    @Operation(summary = "Search skills by name or category")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> searchSkills(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(skillService.searchSkills(search, categoryId)));
    }

    @GetMapping("/explore")
    @Operation(summary = "Explore skills with teacher/learner counts")
    public ResponseEntity<ApiResponse<List<ExploreSkillResponse>>> exploreSkills(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(skillService.exploreSkills(search, categoryId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill details by ID")
    public ResponseEntity<ApiResponse<SkillResponse>> getSkillById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(skillService.getSkillById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new skill")
    public ResponseEntity<ApiResponse<SkillResponse>> createSkill(@Valid @RequestBody CreateSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(skillService.createSkill(request), "Skill created successfully"));
    }
}
