package com.exxeta.shopservice.orders.api.rest.dto;

import java.util.List;

public record UpdateOrderRestDto(List<Integer> productIds) {}
