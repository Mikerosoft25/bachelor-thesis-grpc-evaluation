package com.exxeta.performancetester.clients.shopservice.dto;

import com.exxeta.performancetester.clients.userservice.dto.UserRestDto;
import java.util.List;

public record ProductRecommendationRestDto(UserRestDto user, List<ProductRestDto> recommendedProducts) {}
