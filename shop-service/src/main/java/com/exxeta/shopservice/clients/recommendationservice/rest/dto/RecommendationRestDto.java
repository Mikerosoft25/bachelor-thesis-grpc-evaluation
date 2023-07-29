package com.exxeta.shopservice.clients.recommendationservice.rest.dto;

import java.util.List;

public record RecommendationRestDto(List<String> recommendedCategories) {}
