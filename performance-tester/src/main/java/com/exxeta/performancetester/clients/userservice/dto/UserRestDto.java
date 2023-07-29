package com.exxeta.performancetester.clients.userservice.dto;

public record UserRestDto(
    Integer id,
    String firstName,
    String lastName,
    String postCode,
    String city,
    String address,
    Integer age) {}
