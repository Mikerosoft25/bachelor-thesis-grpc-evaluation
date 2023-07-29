package com.exxeta.shopservice.orders.entity;

import com.exxeta.shopservice.products.entity.Product;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Order {
  @NonNull private Integer id;
  @NonNull private Integer userId;
  @NonNull private LocalDateTime dateTime;
  @NonNull private List<Product> products;
  @NonNull private Float totalPrice;
}
