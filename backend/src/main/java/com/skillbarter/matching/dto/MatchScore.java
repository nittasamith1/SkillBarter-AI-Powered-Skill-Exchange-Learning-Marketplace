package com.skillbarter.matching.dto;

import java.util.Map;

public record MatchScore(
        double totalScorePercent,
        Map<String, Double> breakdownPercent
) {}
