package com.exxeta.performancetester.exception;

public class PerformanceTestException extends RuntimeException {
  public PerformanceTestException(String format, Object... args) {
    super(String.format(format, args));
  }
}
