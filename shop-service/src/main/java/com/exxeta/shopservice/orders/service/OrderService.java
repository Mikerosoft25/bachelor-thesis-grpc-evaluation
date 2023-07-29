package com.exxeta.shopservice.orders.service;

import com.exxeta.shopservice.orders.entity.Order;
import com.exxeta.shopservice.products.entity.Category;
import com.exxeta.shopservice.products.entity.Product;
import com.exxeta.shopservice.products.service.ProductNotFoundException;
import com.exxeta.shopservice.products.service.ProductService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
  private final ProductService productService;

  private final Map<Integer, Order> orders = new ConcurrentHashMap<>();

  public OrderService(ProductService productService) {
    this.productService = productService;

    this.insertMockOrder();
  }

  /**
   * Creates a new order and inserts it into the Map.
   *
   * @param userId the id of the ordering user.
   * @param productIds a list containing all product ids that were ordered.
   * @return the created order.
   * @throws ProductNotFoundException if any product id is not found.
   */
  public Order createOrder(Integer userId, List<Integer> productIds) {
    Float totalPrice = 0f;
    List<Product> products = new ArrayList<>();
    for (int productId : productIds) {
      Product product = this.productService.getProduct(productId);
      products.add(product);

      totalPrice += product.getPrice();
    }

    final Order newOrder =
        Order.builder()
            .id(this.orders.size() + 1)
            .userId(userId)
            .dateTime(LocalDateTime.now())
            .products(products)
            .totalPrice(totalPrice)
            .build();

    orders.put(newOrder.getId(), newOrder);
    return newOrder;
  }

  /**
   * Returns all stored orders.
   *
   * @return a list containing all orders.
   */
  public List<Order> listOrders() {
    return orders.values().stream().toList();
  }

  /**
   * Returns a single order with the given id.
   *
   * @param orderId the id of the order.
   * @return the order with the given id.
   * @throws OrderNotFoundException if the order with the given id is not found.
   */
  public Order getOrder(Integer orderId) {
    final Order order = orders.get(orderId);
    if (order == null) {
      throw new OrderNotFoundException(orderId);
    }

    return order;
  }

  /**
   * Update an order. All parameters except the id are optional and should be set to null if the
   * corresponding field in the user object should not be changed.
   *
   * @param orderId id of the user that will be updated.
   * @param updatedProductIds a list of products belonging to the updated order.
   * @throws OrderNotFoundException if the order with the given id is not found.
   * @throws ProductNotFoundException if the product with the given id is not found.
   */
  public Order updateOrder(Integer orderId, List<Integer> updatedProductIds) {
    Order order = this.getOrder(orderId);

    Float updatedPrice = 0f;
    List<Product> updatedProducts = new ArrayList<>();
    for (int productId : updatedProductIds) {
      Product product = this.productService.getProduct(productId);
      updatedProducts.add(product);

      updatedPrice += product.getPrice();
    }

    order.setProducts(updatedProducts);
    order.setTotalPrice(updatedPrice);

    return order;
  }

  /**
   * Delete an order with the given id.
   *
   * @param orderId the id of the order to delete.
   * @return the deleted order.
   * @throws OrderNotFoundException if the order with the given id is not found.
   */
  public Order deleteOrder(Integer orderId) {
    Order order = orders.remove(orderId);
    if (order == null) {
      throw new OrderNotFoundException(orderId);
    }

    return order;
  }

  /**
   * Returns a list of all categories that the user has bought products from.
   *
   * @param userId The ID of the user.
   * @return a list containing all bought product categories.
   */
  public List<Category> getBoughtCategoriesByUser(Integer userId) {
    List<Order> ordersByUser =
        orders.values().stream().filter(order -> order.getUserId().equals(userId)).toList();

    Set<Category> boughtCategories = new HashSet<>();
    ordersByUser.forEach(
        order ->
            order.getProducts().forEach(product -> boughtCategories.add(product.getCategory())));

    return boughtCategories.stream().toList();
  }

  /**
   * Inserts 100 mock orders to the HashMap that serves as a storage for all orders. Each order
   * consists of 20 products with the IDs from 1 to 20. The first created order is for user with ID
   * 1, the second for user with ID 2 and so on.
   */
  private void insertMockOrder() {
    for (int i = 1; i <= 100; i++) {
      List<Integer> productIds = IntStream.rangeClosed(1, 20).boxed().toList();
      this.createOrder(i, productIds);
    }
  }
}
