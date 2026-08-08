package com.skillbarter.skill.dto;

import java.util.UUID;

public record ExploreSkillResponse(
        UUID id,
        String name,
        String description,
        UUID categoryId,
        String categoryName,
        long teacherCount,
        long learnerCount
) {}
