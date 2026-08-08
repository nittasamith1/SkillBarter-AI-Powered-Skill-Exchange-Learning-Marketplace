package com.skillbarter.skill.dto;

import com.skillbarter.skill.entity.UserSkill;

import java.util.UUID;

public record UserSkillResponse(
        UUID id,
        UUID userId,
        UUID skillId,
        String skillName,
        String categoryName,
        UserSkill.SkillLevel level,
        boolean canTeach,
        boolean wantToLearn,
        Integer yearsExperience
) {
    public static UserSkillResponse from(UserSkill us, String skillName, String categoryName) {
        return new UserSkillResponse(
                us.getId(),
                us.getUserId(),
                us.getSkillId(),
                skillName,
                categoryName,
                us.getLevel(),
                us.isCanTeach(),
                us.isWantToLearn(),
                us.getYearsExperience()
        );
    }
}
