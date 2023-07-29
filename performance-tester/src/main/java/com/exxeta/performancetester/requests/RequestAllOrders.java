package com.exxeta.performancetester.requests;

import com.exxeta.performancetester.clients.shopservice.ShopServiceGrpcClient;
import com.exxeta.performancetester.clients.shopservice.ShopServiceRestClient;
import com.exxeta.performancetester.clients.shopservice.dto.OrderRestDto;
import com.exxeta.shopservice.OrderGrpcDto;
import java.util.List;

/**
 * Class for performance tests where all orders are requested from the Shop-Service.
 */
public class RequestAllOrders implements Request {
  private final ShopServiceGrpcClient shopServiceGrpcClient;
  private final ShopServiceRestClient shopServiceRestClient;

  public RequestAllOrders() {
    this.shopServiceGrpcClient = new ShopServiceGrpcClient();
    this.shopServiceRestClient = new ShopServiceRestClient();
  }

  @Override
  public void executeGrpcRequest() {
    List<OrderGrpcDto> orders = this.shopServiceGrpcClient.listOrders();
    assert orders.size() == 100;
  }

  @Override
  public void executeRestRequest() {
    List<OrderRestDto> orders = this.shopServiceRestClient.listOrders();
    assert orders.size() == 100;
  }
}
