package com.exxeta.shopservice.clients.recommendationservice.grpc;

import com.exxeta.recommendationservice.CreateRecommendationRequest;
import com.exxeta.recommendationservice.RecommendationGrpcDto;
import com.exxeta.recommendationservice.RecommendationServiceGrpc;
import com.exxeta.recommendationservice.RecommendationServiceGrpc.RecommendationServiceBlockingStub;
import com.exxeta.shopservice.products.entity.Category;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!rest")
public class RecommendationServiceGrpcClient {
  private final RecommendationServiceBlockingStub recommendationServiceStub;

  public RecommendationServiceGrpcClient(
      @Value("${client.grpc.recommendationService.hostname}") String hostname,
      @Value("${client.grpc.recommendationService.port}") Integer port) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(hostname, port).usePlaintext().build();

    this.recommendationServiceStub = RecommendationServiceGrpc.newBlockingStub(channel);
  }

  /**
   * Requests category recommendation from the Recommendation-Service.
   *
   * @param boughtCategories Product categories that the user has bought.
   * @param availableCategories Product categories that are available.
   * @return the category recommendations.
   * @throws RuntimeException if the request fails.
   */
  public RecommendationGrpcDto createRecommendation(
      List<Category> boughtCategories, List<Category> availableCategories) {
    List<String> boughtCategoryNames = boughtCategories.stream().map(Category::getName).toList();
    List<String> availableCategoryNames =
        availableCategories.stream().map(Category::getName).toList();

    CreateRecommendationRequest request =
        CreateRecommendationRequest.newBuilder()
            .addAllBoughtCategories(boughtCategoryNames)
            .addAllAvailableCategories(availableCategoryNames)
            .build();

    try {
      return this.recommendationServiceStub.createRecommendation(request);
    } catch (StatusRuntimeException ex) {
      throw new RuntimeException(
          "gRPC-Request for recommendations failed with status code: " + ex.getStatus().getCode());
    }
  }
}
