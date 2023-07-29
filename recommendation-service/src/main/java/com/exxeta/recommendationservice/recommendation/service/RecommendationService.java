package com.exxeta.recommendationservice.recommendation.service;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

  /**
   * Recommends categories based on the bought categories and all available categories. Since this
   * is only a mock implementation, the recommended categories correspond to the bought categories.
   *
   * @param boughtCategories the names of the categories bought by a user.
   * @param availableCategories the names of all available categories.
   * @return the names of the recommended categories.
   */
  public List<String> recommendCategories(
      List<String> boughtCategories, List<String> availableCategories) {
    return boughtCategories;
  }
}
