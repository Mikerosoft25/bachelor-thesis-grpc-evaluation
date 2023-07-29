package com.exxeta.shopservice.orders.service;

public class OrderNotFoundException extends RuntimeException {
  public OrderNotFoundException(Integer id) {
    super(String.format("Order with id '%s' not found", id.toString()));
  }
}
