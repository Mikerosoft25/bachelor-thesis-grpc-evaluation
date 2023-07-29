package com.exxeta.example5.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class ExampleServer {
  public static void main(String[] args) throws InterruptedException, IOException {
    ExampleGrpcService exampleService = new ExampleGrpcService();

    Server server = ServerBuilder.forPort(8080).addService(exampleService).build();
    server.start();
    server.awaitTermination();
  }
}
