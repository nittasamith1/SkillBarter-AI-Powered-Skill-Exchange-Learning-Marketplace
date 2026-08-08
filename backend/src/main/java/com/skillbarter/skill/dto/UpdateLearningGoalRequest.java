package com.skillbarter.skill.dto;

import com.skillbarter.skill.entity.LearningGoal;
import com.skillbarter.skill.entity.UserSkill;

import java.time.LocalDate;

public record UpdateLearningGoalRequest(
        String goalText,
        UserSkill.SkillLevel currentLevel,
        UserSkill.SkillLevel targetLevel,
        LocalDate deadline,
        String learningPreferences,
        LearningGoal.GoalStatus status
) {}
