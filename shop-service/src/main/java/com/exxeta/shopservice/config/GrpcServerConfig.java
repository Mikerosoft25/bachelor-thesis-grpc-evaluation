package com.exxeta.shopservice.config;

import com.exxeta.shopservice.orders.api.grpc.OrderServiceGrpcApi;
import com.exxeta.shopservice.products.api.grpc.ProductServiceGrpcApi;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "server.grpc", name = "enabled", havingValue = "true")
@Configuration
@Slf4j
public class GrpcServerConfig {

  public GrpcServerConfig(
      OrderServiceGrpcApi orderService,
      ProductServiceGrpcApi productService,
      @Value("${server.grpc.port}") Integer grpcPort) {
    Server server =
        ServerBuilder.forPort(grpcPort).addService(orderService).addService(productService).build();

    try {
      server.start();
      log.info("gRPC-Server started on port: {}", grpcPort);
    } catch (IOException ex) {
      throw new RuntimeException("Could not start gRPC-Server on port: " + grpcPort);
    }

    new Thread(
            () -> {
              try {
                server.awaitTermination();
              } catch (InterruptedException e) {
                log.error("gRPC-Server was interrupted");
              }
            })
        .start();
  }
}
