package com.exxeta.shopservice.clients.userservice.grpc;

import com.exxeta.userservice.GetUserRequest;
import com.exxeta.userservice.ListUsersRequest;
import com.exxeta.userservice.UserGrpcDto;
import com.exxeta.userservice.UserListGrpcDto;
import com.exxeta.userservice.UserServiceGrpc;
import com.exxeta.userservice.UserServiceGrpc.UserServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!rest")
public class UserServiceGrpcClient {
  private final UserServiceBlockingStub userServiceStub;

  public UserServiceGrpcClient(
      @Value("${client.grpc.userService.hostname}") String hostname,
      @Value("${client.grpc.userService.port}") Integer port) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(hostname, port).usePlaintext().build();

    this.userServiceStub = UserServiceGrpc.newBlockingStub(channel);
  }

  /**
   * Requests multiple users by their ID from the User-Service.
   *
   * @param userIds the User-IDs of all users to find.
   * @return a list containing all users that the User-Service returned.
   * @throws RuntimeException if the request fails.
   */
  public List<UserGrpcDto> listUsers(List<Integer> userIds) {
    ListUsersRequest request = ListUsersRequest.newBuilder().addAllUserIds(userIds).build();

    try {
      UserListGrpcDto response = userServiceStub.listUsers(request);
      return response.getUsersList();
    } catch (StatusRuntimeException ex) {
      throw new RuntimeException(
          "gRPC-Request for multiple users failed with status code: " + ex.getStatus().getCode());
    }
  }

  /**
   * Requests a single user by its ID from the gRPC-API of the User-Service.
   *
   * @param userId the User-ID of the user to find.
   * @return the found user that the User-Service returned.
   * @throws RuntimeException if the request fails.
   */
  public UserGrpcDto getUser(Integer userId) {
    GetUserRequest request = GetUserRequest.newBuilder().setUserId(userId).build();
    try {
      return userServiceStub.getUser(request);
    } catch (StatusRuntimeException ex) {
      throw new RuntimeException(
          "gRPC-Request for a single user failed with status code: " + ex.getStatus().getCode());
    }
  }
}
