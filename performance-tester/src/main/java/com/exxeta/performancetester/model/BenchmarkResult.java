package com.exxeta.performancetester.model;

public record BenchmarkResult(
    String description,
    int concurrentUsers,
    ApiBenchmarkResult grpcApiBenchmark,
    ApiBenchmarkResult restApiBenchmark) {}
