package com.exxeta.recommendationservice.recommendation.api.spring.dto;

import java.util.List;

public record CreateRecommendationDto(
    List<String> boughtCategories, List<String> availableCategories) {}
