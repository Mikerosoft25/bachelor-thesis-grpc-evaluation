package com.exxeta.performancetester.scenarios.concurrent;

import com.exxeta.performancetester.model.ApiBenchmarkResult;
import com.exxeta.performancetester.model.Microservice;
import com.exxeta.performancetester.requests.Request;
import com.exxeta.performancetester.scenarios.Scenario;
import com.google.common.util.concurrent.RateLimiter;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

/**
 * Simulates a realistic load during the performance test by concurrently executing a defined amount of requests
 * per second for a specified amount of seconds.
 */
public class LoadTestScenario extends Scenario {
  private final Request request;
  private final int durationSeconds;
  private final int requestsPerSecond;

  @Builder
  public LoadTestScenario(
      @NonNull Request request,
      @NonNull Integer durationSeconds,
      @NonNull Integer requestsPerSecond,
      @NonNull String outputFileName,
      @NonNull String description,
      @Singular @NonNull List<Microservice> requiredServices) {
    super(requestsPerSecond, requiredServices, outputFileName, description);
    this.request = request;
    this.durationSeconds = durationSeconds;
    this.requestsPerSecond = requestsPerSecond;
  }

  @Override
  public ApiBenchmarkResult runGrpcApiBenchmark() {
    // warm-up
    for (int i = 1; i <= WARMUP_REQUEST_COUNT; i++) {
      request.executeGrpcRequest();
    }

    Queue<Long> requestDurations = new ConcurrentLinkedQueue<>();
    Runnable runnable =
        () -> {
          try {
            long start = System.nanoTime();
            this.request.executeGrpcRequest();
            long duration = System.nanoTime() - start;

            requestDurations.add(duration);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        };

    long start = System.currentTimeMillis();
    this.runConcurrentRequestsPerSecond(runnable);
    long totalDurationMs = System.currentTimeMillis() - start;

    return new ApiBenchmarkResult(requestDurations.size(), totalDurationMs, requestDurations);
  }

  @Override
  public ApiBenchmarkResult runRestApiBenchmark() {
    // warm-up
    for (int i = 1; i <= WARMUP_REQUEST_COUNT; i++) {
      request.executeRestRequest();
    }

    Queue<Long> requestDurations = new ConcurrentLinkedQueue<>();
    Runnable runnable =
        () -> {
          try {
            long start = System.nanoTime();
            this.request.executeRestRequest();
            long duration = System.nanoTime() - start;

            requestDurations.add(duration);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        };

    long totalDurationMs = this.runConcurrentRequestsPerSecond(runnable);

    return new ApiBenchmarkResult(requestDurations.size(), totalDurationMs, requestDurations);
  }

  private long runConcurrentRequestsPerSecond(Runnable runnable) {
    ExecutorService executor = Executors.newCachedThreadPool();

    int totalRequests = this.requestsPerSecond * this.durationSeconds;
    RateLimiter rateLimiter = RateLimiter.create(this.requestsPerSecond);

    long start = System.currentTimeMillis();
    for (int i = 0; i < totalRequests; i++) {
      rateLimiter.acquire();
      executor.execute(runnable);
    }

    try {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      return System.currentTimeMillis() - start;
    } catch (InterruptedException ex) {
      throw new RuntimeException("ThreadPoolExecutor has been interrupted");
    }
  }

  //  private long runConcurrentRequestsPerSecond(Runnable runnable) {
  //    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1000);
  //
  //    int totalRequests = this.requestsPerSecond * this.durationSeconds;
  //    long delayBetweenRequestsMicro = 1_000_000L / this.requestsPerSecond;
  //
  //    long start = System.currentTimeMillis();
  //    for (int i = 0; i < totalRequests; i++) {
  //      executor.schedule(runnable, i * delayBetweenRequestsMicro, TimeUnit.MICROSECONDS);
  //    }
  //
  //    try {
  //      executor.shutdown();
  //      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
  //      return System.currentTimeMillis() - start;
  //    } catch (InterruptedException ex) {
  //      throw new RuntimeException("ThreadPoolExecutor has been interrupted");
  //    }
  //  }
}
