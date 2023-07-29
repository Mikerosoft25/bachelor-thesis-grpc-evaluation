package com.exxeta.shopservice.clients.recommendationservice.rest.dto;

import java.util.List;

public record CreateRecommendationRestDto(
    List<String> boughtCategories, List<String> availableCategories) {}
