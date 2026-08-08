package com.skillbarter.skill.dto;

import com.skillbarter.skill.entity.UserSkill;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddUserSkillRequest(
        @NotNull(message = "Skill ID is required")
        UUID skillId,

        @NotNull(message = "Skill level is required")
        UserSkill.SkillLevel level,

        boolean canTeach,
        boolean wantToLearn,
        Integer yearsExperience
) {}
