package com.skillbarter.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateSkillRequest(
        @NotBlank(message = "Skill name is required")
        @Size(max = 255, message = "Skill name cannot exceed 255 characters")
        String name,

        String description,

        @NotNull(message = "Category ID is required")
        UUID categoryId,

        List<String> tags,
        List<UUID> prerequisiteSkillIds
) {}
