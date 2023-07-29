package com.exxeta.shopservice.products.service;

import com.exxeta.shopservice.products.entity.Category;
import com.exxeta.shopservice.products.entity.Product;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
  final Map<Integer, Product> products = new ConcurrentHashMap<>();

  public ProductService() {
    this.insertMockProducts();
  }

  /**
   * Creates a new product and inserts it into the Map.
   *
   * @param name The name of the product.
   * @param categoryName the name of the category that the product belongs to.
   * @param price the price of the product.
   * @return the created product.
   * @throws CategoryNotFoundException if the categoryName is invalid.
   */
  public Product createProduct(String name, String categoryName, float price) {
    Category category = Category.fromName(categoryName);

    Product product =
        Product.builder()
            .id(this.products.size() + 1)
            .name(name)
            .category(category)
            .price(price)
            .build();
    this.products.put(product.getId(), product);

    return product;
  }

  /**
   * Returns all stored products.
   *
   * @return a list containing all stored products.
   */
  public List<Product> getAllProducts() {
    return this.products.values().stream().toList();
  }

  /**
   * Returns all products of the specified categories.
   *
   * @param categories the categories of products to be returned.
   * @return a list containing all products of the provided categories.
   */
  public List<Product> getAllProductsOfCategories(List<Category> categories) {
    return this.products.values().stream()
        .filter(product -> categories.contains(product.getCategory()))
        .toList();
  }

  /**
   * Returns a single product with the given id.
   *
   * @param productId the id of the product.
   * @return the product with the given id.
   * @throws ProductNotFoundException if the product with the given id is not found.
   */
  public Product getProduct(Integer productId) {
    Product product = products.get(productId);
    if (product == null) {
      throw new ProductNotFoundException(productId);
    }

    return product;
  }

  /**
   * Update a product. All parameters except the id are optional and should be set to null if the
   * corresponding field in the product object should not be changed.
   *
   * @param productId id of the product that will be updated.
   * @param name updated name of the product.
   * @param categoryName updated category of the product.
   * @param price updated price of the product.
   * @throws ProductNotFoundException if the product with the given id is not found.
   * @throws CategoryNotFoundException if the 'categoryName' is invalid.
   */
  public Product updateProduct(Integer productId, String name, String categoryName, Float price) {
    final Product product = products.get(productId);
    if (product == null) {
      throw new ProductNotFoundException(productId);
    }

    if (name != null) {
      product.setName(name);
    }
    if (categoryName != null) {
      Category category = Category.fromName(categoryName);
      product.setCategory(category);
    }
    if (price != null) {
      product.setPrice(price);
    }

    return product;
  }

  /**
   * Delete a product with the given id.
   *
   * @param productId the id of the product to delete.
   * @return the deleted product.
   * @throws ProductNotFoundException if the product with the given id is not found.
   */
  public Product deleteProduct(Integer productId) {
    Product product = products.remove(productId);
    if (product == null) {
      throw new ProductNotFoundException(productId);
    }

    return product;
  }

  /** Inserts 100 mock products to the HashMap that serves as a storage for all products */
  private void insertMockProducts() {
    for (int i = 1; i <= 100; i++) {
      Category[] categories = Category.values();
      Category category = categories[i % categories.length];

      Product product =
          Product.builder().id(i).name("Product Nr." + i).price(10.00f).category(category).build();
      this.products.put(product.getId(), product);
    }
  }
}
