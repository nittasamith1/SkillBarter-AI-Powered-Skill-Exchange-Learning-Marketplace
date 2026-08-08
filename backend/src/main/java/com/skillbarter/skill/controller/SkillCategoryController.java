package com.skillbarter.skill.controller;

import com.skillbarter.common.response.ApiResponse;
import com.skillbarter.skill.dto.SkillCategoryResponse;
import com.skillbarter.skill.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skill-categories")
@Tag(name = "Skill Categories", description = "Hierarchical skill categories")
public class SkillCategoryController {

    private final SkillService skillService;

    public SkillCategoryController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    @Operation(summary = "Get full category tree")
    public ResponseEntity<ApiResponse<List<SkillCategoryResponse>>> getCategoryTree() {
        return ResponseEntity.ok(ApiResponse.ok(skillService.getCategoryTree()));
    }
}
