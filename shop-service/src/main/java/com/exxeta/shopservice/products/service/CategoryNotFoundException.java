package com.exxeta.shopservice.products.service;

public class CategoryNotFoundException extends RuntimeException {

  /**
   * Constructor with the non-existent category name.
   *
   * @param name the non-existent category name.
   */
  public CategoryNotFoundException(String name) {
    super(String.format("Category with name '%s' not found", name));
  }
}
