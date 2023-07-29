package com.exxeta.shopservice.products.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Product {
  @NonNull private Integer id;
  @NonNull private String name;
  @NonNull private Category category;
  @NonNull private Float price;
}
