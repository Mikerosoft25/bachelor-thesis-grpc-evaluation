package com.exxeta.performancetester.scenarios;

import com.exxeta.performancetester.model.ApiBenchmarkResult;
import com.exxeta.performancetester.model.Microservice;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class Scenario {
  protected static final int WARMUP_REQUEST_COUNT = 300;

  private final int concurrentUsers;
  private final List<Microservice> requiredServices;
  private final String outputFileName;
  private final String description;

  public abstract ApiBenchmarkResult runGrpcApiBenchmark();

  public abstract ApiBenchmarkResult runRestApiBenchmark();
}
