package com.exxeta.userservice.users.api.grpc;

import com.exxeta.userservice.CreateUserRequest;
import com.exxeta.userservice.DeleteUserRequest;
import com.exxeta.userservice.GetUserRequest;
import com.exxeta.userservice.ListUsersRequest;
import com.exxeta.userservice.UpdateUserRequest;
import com.exxeta.userservice.UserGrpcDto;
import com.exxeta.userservice.UserListGrpcDto;
import com.exxeta.userservice.UserServiceGrpc.UserServiceImplBase;
import com.exxeta.userservice.users.entity.User;
import com.exxeta.userservice.users.service.UserNotFoundException;
import com.exxeta.userservice.users.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.springframework.stereotype.Controller;

@Controller
public class UserServiceGrpcApi extends UserServiceImplBase {

  private final UserService userService;

  public UserServiceGrpcApi(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void listUsers(
      ListUsersRequest request, StreamObserver<UserListGrpcDto> responseObserver) {
    List<Integer> userIds = request.getUserIdsList();
    List<User> users =
        userIds.isEmpty() ? userService.getAllUsers() : userService.getAllUsers(userIds);

    UserListGrpcDto response =
        UserListGrpcDto.newBuilder().addAllUsers(this.mapUserListToUserGrpcDtoList(users)).build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getUser(GetUserRequest request, StreamObserver<UserGrpcDto> responseObserver) {
    User user;
    try {
      user = userService.getUser(request.getUserId());
    } catch (UserNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    UserGrpcDto userDto = this.mapUserToUserGrpcDto(user);

    responseObserver.onNext(userDto);
    responseObserver.onCompleted();
  }

  @Override
  public void createUser(CreateUserRequest request, StreamObserver<UserGrpcDto> responseObserver) {
    if (this.isInvalidCreateUserRequest(request)) {
      responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
      return;
    }

    User createdUser =
        userService.createUser(
            request.getFirstName(),
            request.getLastName(),
            request.getAge(),
            request.getPostCode(),
            request.getCity(),
            request.getAddress());

    UserGrpcDto userDto = this.mapUserToUserGrpcDto(createdUser);

    responseObserver.onNext(userDto);
    responseObserver.onCompleted();
  }

  @Override
  public void updateUser(UpdateUserRequest request, StreamObserver<UserGrpcDto> responseObserver) {
    String updatedFirstName = request.hasFirstName() ? request.getFirstName() : null;
    String updatedLastName = request.hasLastName() ? request.getLastName() : null;
    Integer updatedAge = request.hasAge() ? request.getAge() : null;
    String updatedPostCode = request.hasPostCode() ? request.getPostCode() : null;
    String updatedCity = request.hasCity() ? request.getCity() : null;
    String updatedAddress = request.hasAddress() ? request.getAddress() : null;

    User updatedUser;
    try {
      updatedUser =
          userService.updateUser(
              request.getUserId(),
              updatedFirstName,
              updatedLastName,
              updatedAge,
              updatedPostCode,
              updatedCity,
              updatedAddress);
    } catch (UserNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    UserGrpcDto userDto = this.mapUserToUserGrpcDto(updatedUser);

    responseObserver.onNext(userDto);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteUser(DeleteUserRequest request, StreamObserver<UserGrpcDto> responseObserver) {
    User deletedUser;
    try {
      deletedUser = userService.deleteUser(request.getUserId());
    } catch (UserNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    UserGrpcDto userDto = this.mapUserToUserGrpcDto(deletedUser);

    responseObserver.onNext(userDto);
    responseObserver.onCompleted();
  }

  /**
   * Maps the {@link User} object to the generated {@link UserGrpcDto} object that is used as a
   * response type for gRPC requests.
   *
   * @param user the user that should be mapped to the DTO.
   * @return the mapped {@link UserGrpcDto} object.
   */
  private UserGrpcDto mapUserToUserGrpcDto(User user) {
    return UserGrpcDto.newBuilder()
        .setId(user.getId())
        .setFirstName(user.getFirstName())
        .setLastName(user.getLastName())
        .setPostCode(user.getPostCode())
        .setCity(user.getCity())
        .setAddress(user.getAddress())
        .setAge(user.getAge())
        .setDeleted(user.getDeleted())
        .build();
  }

  /**
   * Maps a list of {@link User} objects to a list of {@link UserGrpcDto} objects by calling the
   * {@link #mapUserToUserGrpcDto} method for each user of the list.
   *
   * @param users a list of users that should be mapped.
   * @return a list containing all mapped {@link UserGrpcDto} objects.
   */
  private List<UserGrpcDto> mapUserListToUserGrpcDtoList(List<User> users) {
    return users.stream().map(this::mapUserToUserGrpcDto).toList();
  }

  /**
   * Checks if all parameters of the request to create a new user are valid.
   *
   * @param request the {@link CreateUserRequest}
   * @return true if any parameter is invalid, false if everything is valid.
   */
  private boolean isInvalidCreateUserRequest(CreateUserRequest request) {
    return request.getFirstName().isBlank()
        || request.getLastName().isBlank()
        || request.getAge() <= 0
        || request.getPostCode().isBlank()
        || request.getCity().isBlank()
        || request.getAddress().isBlank();
  }
}
