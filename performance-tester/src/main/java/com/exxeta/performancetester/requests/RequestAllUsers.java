package com.exxeta.performancetester.requests;

import com.exxeta.performancetester.clients.userservice.UserServiceGrpcClient;
import com.exxeta.performancetester.clients.userservice.UserServiceRestClient;
import com.exxeta.performancetester.clients.userservice.dto.UserRestDto;
import com.exxeta.userservice.UserGrpcDto;
import java.util.List;

/**
 * Class for performance tests where all users are requested from the User-Service.
 */
public class RequestAllUsers implements Request {
  private final UserServiceGrpcClient userServiceGrpcClient;
  private final UserServiceRestClient userServiceRestClient;

  public RequestAllUsers() {
    this.userServiceGrpcClient = new UserServiceGrpcClient();
    this.userServiceRestClient = new UserServiceRestClient();
  }

  @Override
  public void executeGrpcRequest() {
    List<UserGrpcDto> users = this.userServiceGrpcClient.listUsers();
    assert users.size() == 1000;
  }

  @Override
  public void executeRestRequest() {
    List<UserRestDto> users = this.userServiceRestClient.listUsers();
    assert users.size() == 1000;
  }
}
