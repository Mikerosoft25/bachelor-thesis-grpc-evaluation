package com.exxeta.performancetester.scenarios.concurrent;

import com.exxeta.performancetester.model.ApiBenchmarkResult;
import com.exxeta.performancetester.model.Microservice;
import com.exxeta.performancetester.requests.Request;
import com.exxeta.performancetester.scenarios.Scenario;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

/**
 * Simulates a stress test during the performance test by concurrently executing a defined amount of
 * requests for a specified amount of seconds.
 */
public class StressTestScenario extends Scenario {
  private final Request request;
  private final int durationSeconds;

  @Builder
  public StressTestScenario(
      @NonNull Request request,
      @NonNull Integer concurrentUsers,
      @NonNull Integer durationSeconds,
      @NonNull String outputFileName,
      @NonNull String description,
      @Singular @NonNull List<Microservice> requiredServices) {
    super(concurrentUsers, requiredServices, outputFileName, description);
    this.request = request;
    this.durationSeconds = durationSeconds;
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
    this.runConcurrentRequestsForDuration(runnable);
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

    long totalDurationMs = this.runConcurrentRequestsForDuration(runnable);

    return new ApiBenchmarkResult(requestDurations.size(), totalDurationMs, requestDurations);
  }

  private long runConcurrentRequestsForDuration(Runnable runnable) {
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1000);

    long end = System.currentTimeMillis() + (this.durationSeconds * 1000L);
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() < end) {
      for (int i = 0; i < this.getConcurrentUsers() - executor.getActiveCount(); i++) {
        executor.submit(runnable);
      }
    }

    try {
      executor.getQueue().clear();
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.SECONDS);
      return System.currentTimeMillis() - start;
    } catch (InterruptedException ex) {
      throw new RuntimeException("ThreadPoolExecutor has been interrupted");
    }
  }
}
