package com.exxeta.userservice.users.api.rest.dto;

public record UserRestDto(
    Integer id,
    String firstName,
    String lastName,
    String postCode,
    String city,
    String address,
    Integer age) {}
