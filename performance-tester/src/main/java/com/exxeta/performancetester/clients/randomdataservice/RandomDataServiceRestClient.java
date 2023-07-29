package com.exxeta.performancetester.clients.randomdataservice;

import com.exxeta.performancetester.clients.randomdataservice.dto.RandomDataRestDto;
import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class RandomDataServiceRestClient {
  private static final String RANDOM_DATA_SERVICE_REST_API_BASE_URL = "http://localhost:8086";

  private final HttpClient httpClient;
  private final Gson gson;

  public RandomDataServiceRestClient() {
    this.httpClient = HttpClient.newHttpClient();
    this.gson = new Gson();
  }

  /**
   * Requests random data from the Random-Data-Service via gRPC API.
   *
   * @param byteCount the amount of bytes that the response should contain.
   * @return the random data string. The length will correspond to the byteCount
   */
  public String getRandomData(int byteCount) {
    URI requestUri =
        URI.create(RANDOM_DATA_SERVICE_REST_API_BASE_URL + "/data?byteCount=" + byteCount);
    HttpRequest request = HttpRequest.newBuilder().uri(requestUri).build();

    try {
      HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new RuntimeException("HTTP-Request failed with code: " + response.statusCode());
      }

      RandomDataRestDto randomDataDto = gson.fromJson(response.body(), RandomDataRestDto.class);
      return randomDataDto.data();
    } catch (Exception ex) {
      throw new RuntimeException("HTTP-Request failed. Error: " + ex.getMessage());
    }
  }
}
