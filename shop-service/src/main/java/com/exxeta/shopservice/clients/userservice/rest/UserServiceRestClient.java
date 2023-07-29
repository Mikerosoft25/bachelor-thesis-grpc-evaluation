package com.exxeta.shopservice.clients.userservice.rest;

import com.exxeta.shopservice.clients.userservice.rest.dto.UserRestDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!grpc")
public class UserServiceRestClient {
  @Value("${client.rest.userService.baseUrl}")
  private String userServiceBaseUrl;

  private final HttpClient httpClient;
  private final Gson gson;

  public UserServiceRestClient() {
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new Gson();
  }

  /**
   * Requests multiple users by their ID from the REST-API of the User-Service.
   *
   * @param userIds the User-IDs of all users to find.
   * @return a list containing all users that the User-Service returned.
   * @throws RuntimeException if the request fails.
   */
  public List<UserRestDto> listUsers(List<Integer> userIds) {
    String commaSeparatedUserIds =
        userIds.stream().map(String::valueOf).collect(Collectors.joining(","));

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(userServiceBaseUrl + "?userIds=" + commaSeparatedUserIds))
            .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, BodyHandlers.ofString());
    } catch (IOException | InterruptedException ex) {
      throw new RuntimeException("Could not execute REST-Request. Error: " + ex.getMessage());
    }

    if (response.statusCode() != 200) {
      throw new RuntimeException(
          "REST-Request for multiple users failed with status code: " + response.statusCode());
    }

    return gson.fromJson(response.body(), new TypeToken<List<UserRestDto>>() {}.getType());
  }

  /**
   * Requests a single user by its ID from the REST-API of the User-Service.
   *
   * @param userId the User-ID of the user to find.
   * @return the found user that the User-Service returned.
   * @throws RuntimeException if the request fails.
   */
  public UserRestDto getUser(Integer userId) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(userServiceBaseUrl + "/" + userId.toString()))
            .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, BodyHandlers.ofString());
    } catch (IOException | InterruptedException ex) {
      throw new RuntimeException("Could not execute REST-Request. Error: " + ex.getMessage());
    }

    if (response.statusCode() != 200) {
      throw new RuntimeException(
          "REST-Request for a single user failed with status code: " + response.statusCode());
    }

    return gson.fromJson(response.body(), UserRestDto.class);
  }
}
