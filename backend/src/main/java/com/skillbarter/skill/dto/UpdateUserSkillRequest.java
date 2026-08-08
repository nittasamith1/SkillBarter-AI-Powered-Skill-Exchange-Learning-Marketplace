package com.skillbarter.skill.dto;

import com.skillbarter.skill.entity.UserSkill;

public record UpdateUserSkillRequest(
        UserSkill.SkillLevel level,
        Boolean canTeach,
        Boolean wantToLearn,
        Integer yearsExperience
) {}
