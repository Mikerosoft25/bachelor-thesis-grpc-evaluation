package com.exxeta.shopservice.products.service;

public class ProductNotFoundException extends RuntimeException {
  public ProductNotFoundException(Integer id) {
    super(String.format("Product with id '%s' not found", id.toString()));
  }
}
