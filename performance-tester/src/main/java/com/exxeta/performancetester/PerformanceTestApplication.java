package com.exxeta.performancetester;

import com.exxeta.performancetester.model.Microservice;
import com.exxeta.performancetester.requests.RequestAllOrders;
import com.exxeta.performancetester.requests.RequestAllUsers;
import com.exxeta.performancetester.requests.RequestRandomData;
import com.exxeta.performancetester.requests.RequestRecommendedProducts;
import com.exxeta.performancetester.scenarios.Scenario;
import com.exxeta.performancetester.scenarios.concurrent.LoadTestScenario;
import com.exxeta.performancetester.scenarios.concurrent.StressTestScenario;
import com.exxeta.performancetester.scenarios.synchronous.SynchronousTestScenario;

public class PerformanceTestApplication {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println(
          "First command line argument must be a file path for the JSON files with the performance test data");
      return;
    }
    String outputDirectory = args[0];

    Scenario scenario1 =
        SynchronousTestScenario.builder()
            .request(new RequestRandomData(10 * 1000))
            .description("Request 10KB random bytes (1000x times)")
            .requestCount(1000)
            .requiredService(Microservice.RANDOM_DATA_SERVICE)
            .outputFileName("sync_1000x_random_data_10kb.json")
            .build();

    Scenario scenario2 =
        SynchronousTestScenario.builder()
            .request(new RequestRandomData(1000 * 1000))
            .description("Request 1MB random bytes (1000x times)")
            .requestCount(1000)
            .requiredService(Microservice.RANDOM_DATA_SERVICE)
            .outputFileName("sync_1000x_random_data_1mb.json")
            .build();

    Scenario scenario3 =
        SynchronousTestScenario.builder()
            .request(new RequestRandomData(10 * 1000 * 1000))
            .description("Request 10MB random bytes (1000x times)")
            .requestCount(1000)
            .requiredService(Microservice.RANDOM_DATA_SERVICE)
            .outputFileName("sync_1000x_random_data_10mb.json")
            .build();

    Scenario scenario4 =
        SynchronousTestScenario.builder()
            .request(new RequestAllUsers())
            .description("Request all users (1000x times)")
            .requestCount(1000)
            .requiredService(Microservice.USER_SERVICE)
            .outputFileName("sync_1000x_all_users.json")
            .build();

    Scenario scenario5 =
        SynchronousTestScenario.builder()
            .request(new RequestAllOrders())
            .description("Request all orders (1000x times)")
            .requestCount(1000)
            .requiredService(Microservice.USER_SERVICE)
            .requiredService(Microservice.SHOP_SERVICE)
            .outputFileName("sync_1000x_all_orders.json")
            .build();

    Scenario scenario6 =
        SynchronousTestScenario.builder()
            .request(new RequestRecommendedProducts())
            .description("Request product recommendations for a single user (1000x times)")
            .requestCount(1000)
            .requiredService(Microservice.USER_SERVICE)
            .requiredService(Microservice.SHOP_SERVICE)
            .requiredService(Microservice.RECOMMENDATION_SERVICE)
            .outputFileName("sync_1000x_product_recommendations.json")
            .build();

    // Load test scenarios with specific amount of requests per second
    Scenario scenario7 =
        LoadTestScenario.builder()
            .request(new RequestAllOrders())
            .description("Request all orders with 300 requests/sec for 15 seconds")
            .requestsPerSecond(300)
            .durationSeconds(15)
            .requiredService(Microservice.USER_SERVICE)
            .requiredService(Microservice.SHOP_SERVICE)
            .outputFileName("load_test_15secs_300ps_request_orders.json")
            .build();

    Scenario scenario8 =
        LoadTestScenario.builder()
            .request(new RequestAllOrders())
            .description("Request all orders with 800 requests/sec for 15 seconds")
            .requestsPerSecond(800)
            .durationSeconds(15)
            .requiredService(Microservice.USER_SERVICE)
            .requiredService(Microservice.SHOP_SERVICE)
            .outputFileName("load_test_15secs_800ps_request_orders.json")
            .build();

    // Stress test scenarios with constant amount of concurrent users
    Scenario scenario9 =
        StressTestScenario.builder()
            .request(new RequestAllOrders())
            .description("Request all orders with constantly 50 concurrent requests for 15 seconds")
            .concurrentUsers(50)
            .durationSeconds(15)
            .requiredService(Microservice.USER_SERVICE)
            .requiredService(Microservice.SHOP_SERVICE)
            .outputFileName("stress_test_15sec_20users_request_orders.json")
            .build();

    Scenario scenario10 =
        StressTestScenario.builder()
            .request(new RequestAllOrders())
            .description(
                "Request all orders with constantly 150 concurrent requests for 15 seconds")
            .concurrentUsers(150)
            .durationSeconds(15)
            .requiredService(Microservice.USER_SERVICE)
            .requiredService(Microservice.SHOP_SERVICE)
            .outputFileName("stress_test_15sec_150users_request_orders.json")
            .build();

    // Add all scenarios and run the performance test.
    PerformanceTestRunner.create(outputDirectory)
        .addScenarios(
            scenario1,
            scenario2,
            scenario3,
            scenario4,
            scenario5,
            scenario6,
            scenario7,
            scenario8,
            scenario9,
            scenario10)
        .run();
  }
}
