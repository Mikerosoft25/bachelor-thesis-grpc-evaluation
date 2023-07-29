package com.exxeta.shopservice.clients.recommendationservice.rest;

import com.exxeta.shopservice.clients.recommendationservice.rest.dto.CreateRecommendationRestDto;
import com.exxeta.shopservice.clients.recommendationservice.rest.dto.RecommendationRestDto;
import com.exxeta.shopservice.products.entity.Category;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!grpc")
public class RecommendationServiceRestClient {
  @Value("${client.rest.recommendationService.baseUrl}")
  private String recommendationServiceBaseUrl;

  private final HttpClient httpClient;
  private final Gson gson;

  public RecommendationServiceRestClient() {
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new Gson();
  }

  public RecommendationRestDto getRecommendedCategories(
      List<Category> boughtCategories, List<Category> availableCategories) {
    List<String> boughtCategoryNames = boughtCategories.stream().map(Category::getName).toList();
    List<String> availableCategoryNames =
        availableCategories.stream().map(Category::getName).toList();

    CreateRecommendationRestDto requestDto =
        new CreateRecommendationRestDto(boughtCategoryNames, availableCategoryNames);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(recommendationServiceBaseUrl))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(gson.toJson(requestDto)))
            .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, BodyHandlers.ofString());
    } catch (IOException | InterruptedException ex) {
      throw new RuntimeException("Could not execute REST-Request");
    }

    if (response.statusCode() != 200) {
      throw new RuntimeException(
          "REST-Request for recommendations failed with status code:" + response.statusCode());
    }

    return gson.fromJson(response.body(), RecommendationRestDto.class);
  }
}
