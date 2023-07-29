package com.exxeta.recommendationservice.recommendation.api.spring.dto;

import java.util.List;

public record RecommendationDto(List<String> recommendedCategories) {}
