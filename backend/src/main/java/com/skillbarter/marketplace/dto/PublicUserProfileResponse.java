package com.skillbarter.marketplace.dto;

import com.skillbarter.skill.dto.LearningGoalResponse;
import com.skillbarter.skill.dto.UserSkillResponse;
import com.skillbarter.user.dto.UserResponse;

import java.util.List;

public record PublicUserProfileResponse(
        UserResponse user,
        List<UserSkillResponse> skillsTeaching,
        List<UserSkillResponse> skillsLearning,
        List<LearningGoalResponse> learningGoals
) {}
