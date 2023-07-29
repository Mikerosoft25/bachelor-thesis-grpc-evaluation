package com.exxeta.recommendationservice.recommendation.api.grpc;

import com.exxeta.recommendationservice.CreateRecommendationRequest;
import com.exxeta.recommendationservice.RecommendationGrpcDto;
import com.exxeta.recommendationservice.RecommendationServiceGrpc.RecommendationServiceImplBase;
import com.exxeta.recommendationservice.recommendation.service.RecommendationService;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RecommendationServiceGrpcApi extends RecommendationServiceImplBase {

  private final RecommendationService recommendationService;

  public RecommendationServiceGrpcApi(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  @Override
  public void createRecommendation(
      CreateRecommendationRequest request, StreamObserver<RecommendationGrpcDto> responseObserver) {
    List<String> recommendedCategories =
        this.recommendationService.recommendCategories(
            request.getBoughtCategoriesList(), request.getAvailableCategoriesList());

    RecommendationGrpcDto response =
        RecommendationGrpcDto.newBuilder()
            .addAllRecommendedCategories(recommendedCategories)
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
