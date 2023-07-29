package com.exxeta.performancetester.clients.userservice;

import com.exxeta.userservice.ListUsersRequest;
import com.exxeta.userservice.UserGrpcDto;
import com.exxeta.userservice.UserListGrpcDto;
import com.exxeta.userservice.UserServiceGrpc;
import com.exxeta.userservice.UserServiceGrpc.UserServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.List;

public class UserServiceGrpcClient {
  private static final String GRPC_API_HOST_NAME = "localhost";
  private static final int USER_SERVICE_GRPC_API_PORT = 8081;

  private final UserServiceBlockingStub userServiceBlockingStub;

  public UserServiceGrpcClient() {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(GRPC_API_HOST_NAME, USER_SERVICE_GRPC_API_PORT)
            .usePlaintext()
            .build();

    this.userServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel);
  }

  /**
   * Requests all users from the User-Service via gRPC API.
   *
   * @return a list containing all users.
   * @throws RuntimeException if the request fails.
   */
  public List<UserGrpcDto> listUsers() {
    ListUsersRequest request = ListUsersRequest.getDefaultInstance();

    try {
      UserListGrpcDto response = this.userServiceBlockingStub.listUsers(request);
      return response.getUsersList();
    } catch (StatusRuntimeException ex) {
      throw new RuntimeException("gRPC-Request failed with code: " + ex.getStatus().getCode());
    }
  }
}
