package com.exxeta.performancetester.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Contains the paths to the jar of the microservices that have to be started for the tests. */
@RequiredArgsConstructor
@Getter
public enum Microservice {
  RANDOM_DATA_SERVICE(
      "Random-Data-Service", "random-data-service/target/random-data-service-1.0.0.jar"),
  USER_SERVICE("User-Service", "user-service/target/user-service-1.0.0.jar"),
  SHOP_SERVICE("Shop-Service", "shop-service/target/shop-service-1.0.0.jar"),
  RECOMMENDATION_SERVICE(
      "Recommendation-Service", "recommendation-service/target/recommendation-service-1.0.0.jar");

  private final String name;
  private final String jarPath;
}
