package com.exxeta.recommendationservice.recommendation.api.spring;

import com.exxeta.recommendationservice.recommendation.api.spring.dto.CreateRecommendationDto;
import com.exxeta.recommendationservice.recommendation.api.spring.dto.RecommendationDto;
import com.exxeta.recommendationservice.recommendation.service.RecommendationService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
public class RecommendationRestApi {

  private final RecommendationService recommendationService;

  public RecommendationRestApi(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  @PostMapping
  public ResponseEntity<RecommendationDto> createRecommendation(
      @RequestBody CreateRecommendationDto createRecommendationDto) {
    List<String> recommendedCategories =
        this.recommendationService.recommendCategories(
            createRecommendationDto.boughtCategories(),
            createRecommendationDto.availableCategories());

    return ResponseEntity.ok(new RecommendationDto(recommendedCategories));
  }
}
