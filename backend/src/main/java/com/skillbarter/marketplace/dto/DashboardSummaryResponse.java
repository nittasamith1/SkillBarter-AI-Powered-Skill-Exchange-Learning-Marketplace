package com.skillbarter.marketplace.dto;

import com.skillbarter.skill.dto.LearningGoalResponse;
import com.skillbarter.skill.dto.UserSkillResponse;

import java.util.List;

public record DashboardSummaryResponse(
        List<UserSkillResponse> mySkills,
        List<LearningGoalResponse> myLearningGoals,
        List<RecommendedMatch> recommendedMatches,
        List<ExchangeRequestResponse> pendingRequests
) {}
