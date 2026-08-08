package com.skillbarter.marketplace.dto;

public record RecommendedMatch(
        PublicUserProfileResponse matchedUser,
        String matchedSkillName,
        String matchReason
) {}
