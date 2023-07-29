package com.exxeta.userservice.users.service;

import com.exxeta.userservice.users.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  final Map<Integer, User> users = new ConcurrentHashMap<>();

  public UserService() {
    this.insertMockUsers();
  }

  /**
   * Create a new user.
   *
   * @param firstName First name of the user.
   * @param lastName Last name of the user.
   * @param age Age of the user.
   * @param postCode Post-Code of the user
   * @param city City that the user lives in.
   * @param address Address of the user.
   * @return the created user.
   */
  public User createUser(
      String firstName,
      String lastName,
      Integer age,
      String postCode,
      String city,
      String address) {
    final User user =
        User.builder()
            .id(this.users.size() + 1)
            .firstName(firstName)
            .lastName(lastName)
            .age(age)
            .postCode(postCode)
            .city(city)
            .address(address)
            .deleted(false)
            .build();

    this.users.put(user.getId(), user);

    return user;
  }

  /**
   * Returns all users.
   *
   * @return List containing all users.
   */
  public List<User> getAllUsers() {
    return users.values().stream().toList();
  }

  /**
   * Returns all users that are matching the supplied IDs.
   *
   * @param userIds ids that should be returned
   * @return a list containing all users that match the supplied IDs.
   */
  public List<User> getAllUsers(List<Integer> userIds) {
    final List<User> users = new ArrayList<>();
    for (Integer userId : userIds) {
      User user = this.users.get(userId);
      if (user != null) {
        users.add(user);
      }
    }

    return users;
  }

  /**
   * Returns a single user with the given id.
   *
   * @param id the id of the user,
   * @return user with the given id
   * @throws UserNotFoundException if the user with the given id is not found.
   */
  public User getUser(Integer id) {
    final User user = users.get(id);
    if (user == null) {
      throw new UserNotFoundException(id);
    }

    return user;
  }

  /**
   * Update a user. All parameters except the id are optional and should be set to null if the
   * corresponding field in the user object should not be changed.
   *
   * @param id id of the user that will be updated.
   * @param firstName updated first name of the user.
   * @param lastName updated last name of the user.
   * @param age updated age of the user.
   * @param postCode updated post code of the user.
   * @param city updated city of the user.
   * @param address updated address of the user.
   * @return the updated user.
   * @throws UserNotFoundException if the user with the given id is not found.
   */
  public User updateUser(
      Integer id,
      String firstName,
      String lastName,
      Integer age,
      String postCode,
      String city,
      String address) {
    final User user = users.get(id);
    if (user == null) {
      throw new UserNotFoundException(id);
    }

    if (firstName != null) {
      user.setFirstName(firstName);
    }
    if (lastName != null) {
      user.setLastName(lastName);
    }
    if (age != null) {
      user.setAge(age);
    }
    if (postCode != null) {
      user.setFirstName(postCode);
    }
    if (city != null) {
      user.setCity(city);
    }
    if (address != null) {
      user.setAddress(address);
    }

    return user;
  }

  /**
   * Delete a user with the given id by marking the user as deleted.
   *
   * @param id the id of the user to delete
   * @return the deleted user
   * @throws UserNotFoundException if the user with the given id is not found
   */
  public User deleteUser(Integer id) {
    final User user = users.remove(id);
    if (user == null) {
      throw new UserNotFoundException(id);
    }

    user.setDeleted(true);
    return user;
  }

  /** Inserts 1000 mock users to the HashMap that serves as a storage for all users */
  private void insertMockUsers() {
    for (int id = 1; id <= 1000; id++) {
      this.users.put(
          id,
          User.builder()
              .id(id)
              .firstName("User")
              .lastName("Nr. " + id)
              .postCode("12345")
              .city("Randomcity")
              .address("Random-Street " + id)
              .age(20)
              .deleted(false)
              .build());
    }
  }
}
