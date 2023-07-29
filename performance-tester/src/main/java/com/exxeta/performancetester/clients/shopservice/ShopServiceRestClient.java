package com.exxeta.performancetester.clients.shopservice;

import com.exxeta.performancetester.clients.shopservice.dto.OrderRestDto;
import com.exxeta.performancetester.clients.shopservice.dto.ProductRecommendationRestDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

public class ShopServiceRestClient {
  private static final String SHOP_SERVICE_REST_API_BASE_URL = "http://localhost:8082";

  private final HttpClient httpClient;
  private final Gson gson;

  public ShopServiceRestClient() {
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new Gson();
  }

  /**
   * Requests all orders from the Shop-Service via REST API.
   *
   * @return a list containing all orders.
   * @throws RuntimeException if the request fails.
   */
  public List<OrderRestDto> listOrders() {
    URI requestUri = URI.create(SHOP_SERVICE_REST_API_BASE_URL + "/orders");
    HttpRequest request = HttpRequest.newBuilder().uri(requestUri).build();

    try {
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new RuntimeException(
            "HTTP-Request failed with code: " + response.statusCode() + ", " + response.body());
      }

      return gson.fromJson(response.body(), new TypeToken<ArrayList<OrderRestDto>>() {}.getType());
    } catch (Exception ex) {
      throw new RuntimeException("HTTP-Request failed. Error: " + ex.getMessage());
    }
  }

  /**
   * Requests recommended products from the Shop-Service via REST-API.
   *
   * @param userId the id of the user for whom the products are recommended.
   * @return the product recommendations.
   * @throws RuntimeException if the request fails.
   */
  public ProductRecommendationRestDto getProductRecommendation(int userId) {
    URI requestUri = URI.create(SHOP_SERVICE_REST_API_BASE_URL + "/products/recommended/" + userId);
    HttpRequest request = HttpRequest.newBuilder().uri(requestUri).build();

    try {
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new RuntimeException("HTTP-Request failed with code: " + response.statusCode());
      }

      return gson.fromJson(response.body(), ProductRecommendationRestDto.class);
    } catch (Exception ex) {
      throw new RuntimeException("HTTP-Request failed. Error: " + ex.getMessage());
    }
  }
}
