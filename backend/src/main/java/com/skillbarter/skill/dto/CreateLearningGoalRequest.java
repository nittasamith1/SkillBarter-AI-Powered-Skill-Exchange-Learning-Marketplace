package com.skillbarter.skill.dto;

import com.skillbarter.skill.entity.UserSkill;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateLearningGoalRequest(
        @NotNull(message = "Target skill ID is required")
        UUID targetSkillId,

        String goalText,
        UserSkill.SkillLevel currentLevel,
        UserSkill.SkillLevel targetLevel,
        LocalDate deadline,
        String learningPreferences
) {}
