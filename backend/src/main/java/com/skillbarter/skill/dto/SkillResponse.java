package com.skillbarter.skill.dto;

import com.skillbarter.skill.entity.Skill;

import java.util.List;
import java.util.UUID;

public record SkillResponse(
        UUID id,
        String name,
        String description,
        UUID categoryId,
        String categoryName,
        boolean isGlobal,
        String tags,
        List<SkillResponse> prerequisites
) {
    public static SkillResponse from(Skill skill, String categoryName, List<SkillResponse> prerequisites) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getDescription(),
                skill.getCategoryId(),
                categoryName,
                skill.isGlobal(),
                skill.getTags(),
                prerequisites != null ? prerequisites : List.of()
        );
    }
}
