package com.exxeta.userservice.users.api.rest.dto;

public record UpdateUserRestDto(
    String firstName, String lastName, String postCode, String city, String address, Integer age) {}
