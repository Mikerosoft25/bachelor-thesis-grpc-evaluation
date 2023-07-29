package com.exxeta.shopservice.orders.api.rest.dto;

import com.exxeta.shopservice.clients.userservice.rest.dto.UserRestDto;
import com.exxeta.shopservice.products.api.rest.dto.ProductRestDto;
import java.util.List;

public record OrderRestDto(
    Integer id,
    String isoTimestamp,
    List<ProductRestDto> products,
    Float totalPrice,
    UserRestDto user) {}
