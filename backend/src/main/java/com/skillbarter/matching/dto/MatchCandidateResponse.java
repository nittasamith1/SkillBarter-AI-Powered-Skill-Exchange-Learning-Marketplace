package com.skillbarter.matching.dto;

import com.skillbarter.marketplace.dto.PublicUserProfileResponse;

public record MatchCandidateResponse(
        PublicUserProfileResponse candidateProfile,
        MatchScore score,
        String matchedSkillName,
        String matchReason
) {}
