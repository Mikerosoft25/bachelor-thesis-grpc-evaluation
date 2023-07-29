package com.exxeta.performancetester.clients.shopservice;

import com.exxeta.shopservice.ListRecommendedProductsRequest;
import com.exxeta.shopservice.OrderGrpcDto;
import com.exxeta.shopservice.OrderListGrpcDto;
import com.exxeta.shopservice.OrderServiceGrpc;
import com.exxeta.shopservice.OrderServiceGrpc.OrderServiceBlockingStub;
import com.exxeta.shopservice.ProductRecommendationGrpcDto;
import com.exxeta.shopservice.ProductServiceGrpc;
import com.exxeta.shopservice.ProductServiceGrpc.ProductServiceBlockingStub;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.List;

public class ShopServiceGrpcClient {

  private static final String GRPC_API_HOST_NAME = "localhost";
  private static final int SHOP_SERVICE_GRPC_API_PORT = 8083;

  private final OrderServiceBlockingStub orderServiceBlockingStub;
  private final ProductServiceBlockingStub productServiceBlockingStub;

  public ShopServiceGrpcClient() {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(GRPC_API_HOST_NAME, SHOP_SERVICE_GRPC_API_PORT)
            .usePlaintext()
            .build();

    this.orderServiceBlockingStub = OrderServiceGrpc.newBlockingStub(channel);
    this.productServiceBlockingStub = ProductServiceGrpc.newBlockingStub(channel);
  }

  /**
   * Requests all orders from the Shop-Service via gRPC API.
   *
   * @return a list containing all orders.
   * @throws RuntimeException if the request fails.
   */
  public List<OrderGrpcDto> listOrders() {
    Empty emptyRequest = Empty.newBuilder().build();
    try {
      OrderListGrpcDto response = this.orderServiceBlockingStub.listOrders(emptyRequest);
      return response.getOrdersList();
    } catch (StatusRuntimeException ex) {
      throw new RuntimeException("gRPC-Request failed with code: " + ex.getStatus().getCode());
    }
  }

  /**
   * Requests recommended products from the Shop-Service via gRPC-API.
   *
   * @param userId the id of the user for whom the products are recommended.
   * @return the product recommendations.
   * @throws RuntimeException if the request fails.
   */
  public ProductRecommendationGrpcDto getProductRecommendation(int userId) {
    ListRecommendedProductsRequest request =
        ListRecommendedProductsRequest.newBuilder().setUserId(userId).build();
    try {
      return this.productServiceBlockingStub.listRecommendedProducts(request);
    } catch (StatusRuntimeException ex) {
      throw new RuntimeException("gRPC-Request failed with code: " + ex.getStatus().getCode());
    }
  }
}
