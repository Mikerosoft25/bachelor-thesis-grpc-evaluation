package com.exxeta.userservice.users.api.rest;

import com.exxeta.userservice.CreateUserRequest;
import com.exxeta.userservice.users.api.rest.dto.CreateUserRestDto;
import com.exxeta.userservice.users.api.rest.dto.UpdateUserRestDto;
import com.exxeta.userservice.users.api.rest.dto.UserRestDto;
import com.exxeta.userservice.users.entity.User;
import com.exxeta.userservice.users.service.UserNotFoundException;
import com.exxeta.userservice.users.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserServiceRestApi {

  private final UserService userService;

  public UserServiceRestApi(final UserService userService) {
    this.userService = userService;
  }

  @PostMapping()
  public ResponseEntity<UserRestDto> createUser(@RequestBody CreateUserRestDto createUserDto) {
    if (this.isInvalidCreateUserRequest(createUserDto)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    User user =
        this.userService.createUser(
            createUserDto.firstName(),
            createUserDto.lastName(),
            createUserDto.age(),
            createUserDto.postCode(),
            createUserDto.city(),
            createUserDto.address());

    UserRestDto userDto = this.mapUserToUserRestDto(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  @GetMapping
  public ResponseEntity<List<UserRestDto>> listUsers() {
    List<User> users = this.userService.getAllUsers();

    List<UserRestDto> userDtoList = this.mapUserListToUserRestDtoList(users);
    return ResponseEntity.ok(userDtoList);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserRestDto> getUser(@PathVariable Integer userId) {
    try {
      User user = this.userService.getUser(userId);

      UserRestDto userDto = this.mapUserToUserRestDto(user);
      return ResponseEntity.ok(userDto);
    } catch (UserNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<UserRestDto> updateUser(
      @PathVariable Integer userId, @RequestBody UpdateUserRestDto updateUserDto) {
    User user =
        this.userService.updateUser(
            userId,
            updateUserDto.firstName(),
            updateUserDto.lastName(),
            updateUserDto.age(),
            updateUserDto.postCode(),
            updateUserDto.city(),
            updateUserDto.address());

    UserRestDto userDto = this.mapUserToUserRestDto(user);
    return ResponseEntity.ok(userDto);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<UserRestDto> deleteUser(@PathVariable Integer userId) {
    try {
      User deletedUser = this.userService.deleteUser(userId);

      UserRestDto userDto = this.mapUserToUserRestDto(deletedUser);
      return ResponseEntity.ok(userDto);
    } catch (UserNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }
  }

  /**
   * Maps the {@link User} object to the {@link UserRestDto} object that is used as a response type
   * for REST requests.
   *
   * @param user the user that should be mapped to the DTO.
   * @return the mapped {@link UserRestDto} object.
   */
  private UserRestDto mapUserToUserRestDto(User user) {
    return new UserRestDto(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getPostCode(),
        user.getCity(),
        user.getAddress(),
        user.getAge());
  }

  /**
   * Maps a list of {@link User} objects to a list of {@link UserRestDto} objects by calling the
   * {@link #mapUserToUserRestDto} method for each user of the list.
   *
   * @param users a list of users that should be mapped.
   * @return a list containing all mapped {@link UserRestDto} objects.
   */
  private List<UserRestDto> mapUserListToUserRestDtoList(List<User> users) {
    return users.stream().map(this::mapUserToUserRestDto).toList();
  }

  /**
   * Checks if all parameters of the request to create a new user are valid.
   *
   * @param request the {@link CreateUserRequest}
   * @return true if any parameter is invalid, false if everything is valid.
   */
  private boolean isInvalidCreateUserRequest(CreateUserRestDto request) {
    return request.firstName().isBlank()
        || request.lastName().isBlank()
        || request.age() <= 0
        || request.postCode().isBlank()
        || request.city().isBlank()
        || request.address().isBlank();
  }
}
