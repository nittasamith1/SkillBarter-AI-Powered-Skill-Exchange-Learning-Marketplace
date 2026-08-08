package com.skillbarter.skill.dto;

import com.skillbarter.skill.entity.SkillCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SkillCategoryResponse(
        UUID id,
        String name,
        String description,
        UUID parentId,
        List<SkillCategoryResponse> children
) {
    public static SkillCategoryResponse from(SkillCategory category, List<SkillCategoryResponse> children) {
        return new SkillCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                children != null ? children : new ArrayList<>()
        );
    }
}
