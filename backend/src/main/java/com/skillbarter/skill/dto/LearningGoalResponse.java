package com.skillbarter.skill.dto;

import com.skillbarter.skill.entity.LearningGoal;
import com.skillbarter.skill.entity.UserSkill;

import java.time.LocalDate;
import java.util.UUID;

public record LearningGoalResponse(
        UUID id,
        UUID userId,
        UUID targetSkillId,
        String targetSkillName,
        String goalText,
        UserSkill.SkillLevel currentLevel,
        UserSkill.SkillLevel targetLevel,
        LocalDate deadline,
        String learningPreferences,
        LearningGoal.GoalStatus status
) {
    public static LearningGoalResponse from(LearningGoal lg, String targetSkillName) {
        return new LearningGoalResponse(
                lg.getId(),
                lg.getUserId(),
                lg.getTargetSkillId(),
                targetSkillName,
                lg.getGoalText(),
                lg.getCurrentLevel(),
                lg.getTargetLevel(),
                lg.getDeadline(),
                lg.getLearningPreferences(),
                lg.getStatus()
        );
    }
}
