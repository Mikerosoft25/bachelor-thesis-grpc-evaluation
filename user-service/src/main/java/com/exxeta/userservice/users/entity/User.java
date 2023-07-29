package com.exxeta.userservice.users.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
@Getter
@Setter
public class User {
  @NonNull private Integer id;
  @NonNull private String firstName;
  @NonNull private String lastName;
  @NonNull private String postCode;
  @NonNull private String city;
  @NonNull private String address;
  @NonNull private Integer age;
  @NonNull private Boolean deleted;
}
