package com.exxeta.performancetester.clients.userservice;

import com.exxeta.performancetester.clients.userservice.dto.UserRestDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

public class UserServiceRestClient {
  private static final String USER_SERVICE_REST_API_BASE_URL = "http://localhost:8080";

  private final HttpClient httpClient;
  private final Gson gson;

  public UserServiceRestClient() {
    this.httpClient = HttpClient.newBuilder().version(Version.HTTP_2).build();
    this.gson = new Gson();
  }

  /**
   * Requests all users from the User-Service via REST API.
   *
   * @return a list containing all users.
   * @throws RuntimeException if the request fails.
   */
  public List<UserRestDto> listUsers() {
    URI requestUri = URI.create(USER_SERVICE_REST_API_BASE_URL + "/users");
    HttpRequest request = HttpRequest.newBuilder().uri(requestUri).build();

    try {
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new RuntimeException("HTTP-Request failed with code: " + response.statusCode());
      }

      return gson.fromJson(response.body(), new TypeToken<ArrayList<UserRestDto>>() {}.getType());
    } catch (Exception ex) {
      throw new RuntimeException("HTTP-Request failed. Error: " + ex.getClass().getSimpleName());
    }
  }
}
