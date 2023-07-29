package com.exxeta.performancetester;

import com.exxeta.performancetester.exception.PerformanceTestException;
import com.exxeta.performancetester.model.ApiBenchmarkResult;
import com.exxeta.performancetester.model.BenchmarkResult;
import com.exxeta.performancetester.model.Microservice;
import com.exxeta.performancetester.scenarios.Scenario;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceTestRunner {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final String REST_API_PROFILE = "rest";
  private static final String GRPC_API_PROFILE = "grpc";

  private static final String APPLICATION_STARTED_PATTERN =
      ".*Started (.+)Application in (.+) seconds.*";

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private final String jsonOutputDirectory;
  private final List<Scenario> testScenarios = new ArrayList<>();
  private final List<Process> runningProcesses = new ArrayList<>();

  private PerformanceTestRunner(String jsonOutputDirectory) {
    this.jsonOutputDirectory = jsonOutputDirectory;

    Runtime.getRuntime().addShutdownHook(new Thread(this::stopAllMicroservices));
  }

  /**
   * Creates a new performance test.
   *
   * @param outputDirectory directory where all performance test data will be written.
   * @return the created {@link PerformanceTestRunner}
   */
  public static PerformanceTestRunner create(String outputDirectory) {
    return new PerformanceTestRunner(outputDirectory);
  }

  /**
   * Add a scenario to the performance test runner.
   *
   * @param scenario the performance test scenario to run.
   * @return this instance of the {@link PerformanceTestRunner}
   */
  public PerformanceTestRunner addScenario(Scenario scenario) {
    this.testScenarios.add(scenario);
    return this;
  }

  /**
   * Add multiple scenarios to the performance test runner.
   *
   * @param scenarios multiple performance test scenarios to run.
   * @return this instance of the {@link PerformanceTestRunner}
   */
  public PerformanceTestRunner addScenarios(Scenario... scenarios) {
    this.testScenarios.addAll(List.of(scenarios));
    return this;
  }

  /** Executes the performance tests with all configured scenarios. */
  public void run() {
    try {
      int index = 1;
      for (Scenario scenario : this.testScenarios) {
        logger.info("------------------------------------------------------------------------");
        logger.info(
            "Running scenario '{}' ({}/{})",
            scenario.getDescription(),
            index++,
            this.testScenarios.size());

        this.stopAllMicroservices();
        this.startMicroservices(scenario.getRequiredServices(), GRPC_API_PROFILE);
        logger.info("Starting the gRPC-API Benchmark");
        ApiBenchmarkResult grpcResult = scenario.runGrpcApiBenchmark();
        logger.info("Finished the gRPC-API Benchmark");

        this.stopAllMicroservices();
        this.startMicroservices(scenario.getRequiredServices(), REST_API_PROFILE);
        logger.info("Starting the REST-API Benchmark");
        ApiBenchmarkResult restResult = scenario.runRestApiBenchmark();
        logger.info("Finished the REST-API Benchmark");

        BenchmarkResult benchmarkResult =
            new BenchmarkResult(
                scenario.getDescription(), scenario.getConcurrentUsers(), grpcResult, restResult);
        this.writeBenchmarkToFile(benchmarkResult, scenario.getOutputFileName());
      }
    } catch (Exception ex) {
      logger.error("Test runner failed with error: {}", ex.getMessage());
      this.stopAllMicroservices();
      System.exit(1);
    } finally {
      this.stopAllMicroservices();
    }
  }

  /**
   * Starts the required microservices for the performance test.
   *
   * @param servicesToStart a list containing every {@link Microservice} that needs to be started.
   * @param profile the profile with which the microservice should be started.
   */
  private void startMicroservices(List<Microservice> servicesToStart, String profile) {
    for (Microservice microservice : servicesToStart) {
      logger.info(
          "Starting Microservice: '{}' with profile: '{}'", microservice.getName(), profile);
      Process process = this.startMicroservice(profile, microservice.getJarPath());
      this.runningProcesses.add(process);
    }
    logger.info("Successfully started all required Microservices for the current test-scenario");
  }

  /**
   * Starts a single microservice the provided profile.
   *
   * @param profile the profile with which the microservice should be started.
   * @param jarPath the path to the jar of the microservice.
   * @return the {@link Process} of the started microservice.
   * @throws PerformanceTestException if the microservice could not be successfully started.
   */
  private Process startMicroservice(String profile, String jarPath) {
    String activeProfile = "-Dspring.profiles.active=" + profile;
    String cmd = String.format("java -jar %s %s", activeProfile, jarPath);

    try {
      Process process = Runtime.getRuntime().exec(cmd);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

      boolean successfulStart = false;

      String processOutput;
      while ((processOutput = stdInput.readLine()) != null) {
        if (processOutput.matches(APPLICATION_STARTED_PATTERN)) {
          successfulStart = true;
          break;
        }
      }

      if (!successfulStart) {
        throw new PerformanceTestException("Process for command '%s' did not start correctly", cmd);
      }

      if (this.processIsTerminated(process)) {
        throw new PerformanceTestException(
            "Process for command '%s' exited with code: %d", cmd, process.exitValue());
      }

      return process;
    } catch (IOException ex) {
      throw new PerformanceTestException(
          "Could not read StdOutput of process for command: '%s'", cmd);
    }
  }

  /**
   * Checks if a process is terminated.
   *
   * @param process the process to check.
   * @return true if the process terminated, false otherwise.
   */
  private boolean processIsTerminated(Process process) {
    try {
      process.exitValue();
    } catch (IllegalThreadStateException ex) {
      return false;
    }
    return true;
  }

  /** Stops all microservices. */
  private void stopAllMicroservices() {
    logger.info("Stopping all running Microservices");
    for (Process process : this.runningProcesses) {
      process.destroy();
    }
    this.runningProcesses.clear();
  }

  /**
   * Writes all results of a performance test to a JSON file.
   *
   * @param benchmarkResult the performance test result of a scenario.
   * @param fileName The file name of the JSON File that will be written.
   */
  private void writeBenchmarkToFile(BenchmarkResult benchmarkResult, String fileName) {
    try {
      Files.createDirectories(Path.of(this.jsonOutputDirectory));
    } catch (IOException ex) {
      throw new PerformanceTestException(
          "Could not create directory: '%s'", this.jsonOutputDirectory);
    }

    String filePath = this.jsonOutputDirectory + "/" + fileName;
    try (FileWriter fw = new FileWriter(filePath)) {
      logger.info("Writing Benchmark result to '{}'", filePath);
      gson.toJson(benchmarkResult, fw);
    } catch (JsonIOException | IOException ex) {
      throw new PerformanceTestException("Could not write benchmark to file: '%s'", filePath);
    }
  }
}
