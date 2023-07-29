package com.exxeta.performancetester.clients.randomdataservice;

import com.exxeta.randomdataservice.GetRandomDataRequest;
import com.exxeta.randomdataservice.RandomDataGrpcDto;
import com.exxeta.randomdataservice.RandomDataServiceGrpc;
import com.exxeta.randomdataservice.RandomDataServiceGrpc.RandomDataServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class RandomDataServiceGrpcClient {
  private static final String GRPC_API_HOST_NAME = "localhost";
  private static final int RANDOM_DATA_SERVICE_GRPC_API_PORT = 8087;

  private final RandomDataServiceBlockingStub randomDataServiceBlockingStub;

  public RandomDataServiceGrpcClient() {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(GRPC_API_HOST_NAME, RANDOM_DATA_SERVICE_GRPC_API_PORT)
            .usePlaintext()
            .maxInboundMessageSize(100_000_000)
            .build();

    this.randomDataServiceBlockingStub = RandomDataServiceGrpc.newBlockingStub(channel);
  }

  /**
   * Requests random data from the Random-Data-Service via REST API.
   *
   * @param byteCount the amount of bytes that the response should contain.
   * @return the random data string. The length will correspond to the byteCount
   */
  public String getRandomData(int byteCount) {
    GetRandomDataRequest request =
        GetRandomDataRequest.newBuilder().setByteCount(byteCount).build();
    try {
      RandomDataGrpcDto response = this.randomDataServiceBlockingStub.getRandomData(request);
      return response.getData();
    } catch (StatusRuntimeException ex) {
      throw new RuntimeException("gRPC-Request failed with code: " + ex.getStatus().getCode());
    }
  }
}
