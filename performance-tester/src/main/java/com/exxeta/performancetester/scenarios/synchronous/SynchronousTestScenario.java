package com.exxeta.performancetester.scenarios.synchronous;

import com.exxeta.performancetester.model.ApiBenchmarkResult;
import com.exxeta.performancetester.model.Microservice;
import com.exxeta.performancetester.requests.Request;
import com.exxeta.performancetester.scenarios.Scenario;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

/** Synchronous scenario for the performance tests that executes requests one after another. */
public class SynchronousTestScenario extends Scenario {
  private final Request request;
  private final int requestCount;

  @Builder
  public SynchronousTestScenario(
      @NonNull Request request,
      @NonNull Integer requestCount,
      @NonNull String outputFileName,
      @NonNull String description,
      @Singular @NonNull List<Microservice> requiredServices) {
    super(1, requiredServices, outputFileName, description);
    this.request = request;
    this.requestCount = requestCount;
  }

  @Override
  public ApiBenchmarkResult runGrpcApiBenchmark() {
    // warm-up
    for (int i = 1; i <= WARMUP_REQUEST_COUNT; i++) {
      request.executeGrpcRequest();
    }

    List<Long> durations = new ArrayList<>(this.requestCount);
    long totalStart = System.currentTimeMillis();
    for (int i = 1; i <= this.requestCount; i++) {
      long start = System.nanoTime();
      request.executeGrpcRequest();
      long duration = System.nanoTime() - start;

      durations.add(duration);
    }
    long totalDurationMs = System.currentTimeMillis() - totalStart;

    return new ApiBenchmarkResult(this.requestCount, totalDurationMs, durations);
  }

  @Override
  public ApiBenchmarkResult runRestApiBenchmark() {
    // warm-up
    for (int i = 1; i <= WARMUP_REQUEST_COUNT; i++) {
      request.executeRestRequest();
    }

    List<Long> durations = new ArrayList<>(this.requestCount);
    long totalStart = System.currentTimeMillis();
    for (int i = 1; i <= this.requestCount; i++) {
      long start = System.nanoTime();
      request.executeRestRequest();
      long duration = System.nanoTime() - start;

      durations.add(duration);
    }
    long totalDurationMs = System.currentTimeMillis() - totalStart;

    return new ApiBenchmarkResult(this.requestCount, totalDurationMs, durations);
  }
}
