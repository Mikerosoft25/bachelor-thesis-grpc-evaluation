package com.exxeta.performancetester.requests;

import com.exxeta.performancetester.clients.shopservice.ShopServiceGrpcClient;
import com.exxeta.performancetester.clients.shopservice.ShopServiceRestClient;
import com.exxeta.performancetester.clients.shopservice.dto.ProductRecommendationRestDto;
import com.exxeta.shopservice.ProductRecommendationGrpcDto;

/**
 * Class for performance tests where recommended products are requested from the Shop-Service.
 */
public class RequestRecommendedProducts implements Request {
  private final ShopServiceGrpcClient shopServiceGrpcClient;
  private final ShopServiceRestClient shopServiceRestClient;

  public RequestRecommendedProducts() {
    this.shopServiceGrpcClient = new ShopServiceGrpcClient();
    this.shopServiceRestClient = new ShopServiceRestClient();
  }

  @Override
  public void executeGrpcRequest() {
    ProductRecommendationGrpcDto response = this.shopServiceGrpcClient.getProductRecommendation(1);
    assert response.getRecommendedProductsList().size() == 100;
  }

  @Override
  public void executeRestRequest() {
    ProductRecommendationRestDto response = this.shopServiceRestClient.getProductRecommendation(1);
    assert response.recommendedProducts().size() == 100;
  }
}
