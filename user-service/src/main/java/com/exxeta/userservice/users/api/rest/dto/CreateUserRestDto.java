package com.exxeta.userservice.users.api.rest.dto;

public record CreateUserRestDto(
    String firstName, String lastName, String postCode, String city, String address, Integer age) {}
