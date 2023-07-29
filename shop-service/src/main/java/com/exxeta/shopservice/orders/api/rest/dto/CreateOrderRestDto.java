package com.exxeta.shopservice.orders.api.rest.dto;

import java.util.List;

public record CreateOrderRestDto(Integer userId, List<Integer> productIds) {}
