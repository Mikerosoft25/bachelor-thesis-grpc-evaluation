package com.exxeta.performancetester.requests;

import com.exxeta.performancetester.clients.randomdataservice.RandomDataServiceGrpcClient;
import com.exxeta.performancetester.clients.randomdataservice.RandomDataServiceRestClient;

/**
 * Class for performance tests where random data is requested from the Random-Data-Service.
 */
public class RequestRandomData implements Request {
  private final RandomDataServiceGrpcClient randomDataServiceGrpcClient;
  private final RandomDataServiceRestClient randomDataServiceRestClient;

  private final int byteCount;

  public RequestRandomData(int byteCount) {
    this.randomDataServiceGrpcClient = new RandomDataServiceGrpcClient();
    this.randomDataServiceRestClient = new RandomDataServiceRestClient();
    this.byteCount = byteCount;
  }

  @Override
  public void executeGrpcRequest() {
    String response = this.randomDataServiceGrpcClient.getRandomData(byteCount);
    assert response.length() == byteCount;
  }

  @Override
  public void executeRestRequest() {
    String response = this.randomDataServiceRestClient.getRandomData(byteCount);
    assert response.length() == byteCount;
  }
}
