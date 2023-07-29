package com.exxeta.performancetester.model;

import java.util.Collection;

public record ApiBenchmarkResult(
    int requestCount, long totalDurationMs, Collection<Long> requestDurationNanos) {}
