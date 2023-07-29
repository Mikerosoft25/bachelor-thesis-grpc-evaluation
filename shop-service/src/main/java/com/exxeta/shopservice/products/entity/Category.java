package com.exxeta.shopservice.products.entity;

import com.exxeta.shopservice.products.service.CategoryNotFoundException;
import java.util.List;
import lombok.Getter;

@Getter
public enum Category {
  CATEGORY_1("Category 1"),
  CATEGORY_2("Category 2"),
  CATEGORY_3("Category 3"),
  CATEGORY_4("Category 4"),
  CATEGORY_5("Category 5"),
  CATEGORY_6("Category 6"),
  CATEGORY_7("Category 7"),
  CATEGORY_8("Category 8"),
  CATEGORY_9("Category 9"),
  CATEGORY_10("Category 10");

  private final String name;

  Category(String name) {
    this.name = name;
  }

  /**
   * Returns the {@link Category} with the given name.
   *
   * @param name the name of the category.
   * @return the category.
   * @throws CategoryNotFoundException if the category name is invalid.
   */
  public static Category fromName(String name) {
    for (Category category : Category.values()) {
      if (category.getName().equalsIgnoreCase(name)) {
        return category;
      }
    }

    throw new CategoryNotFoundException(name);
  }
}
