package com.exxeta.performancetester.clients.shopservice.dto;

import com.exxeta.performancetester.clients.userservice.dto.UserRestDto;
import java.util.List;

public record OrderRestDto(
    Integer id, String isoTimestamp, List<ProductRestDto> products, Float totalPrice, UserRestDto user) {}
