package com.exxeta.shopservice.products.api.rest.dto;

import com.exxeta.shopservice.clients.userservice.rest.dto.UserRestDto;
import java.util.List;

public record ProductRecommendationRestDto(UserRestDto user, List<ProductRestDto> recommendedProducts) {}
