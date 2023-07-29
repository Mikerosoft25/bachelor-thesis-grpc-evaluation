package com.exxeta.example5.server;

import com.exxeta.protos.example5.ExampleServiceGrpc.ExampleServiceImplBase;
import com.exxeta.protos.example5.RequestMessage;
import com.exxeta.protos.example5.ResponseMessage;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * Implements all four types of RPC in gRPC by extending and overriding the generated default
 * implementation.
 */
public class ExampleGrpcService extends ExampleServiceImplBase {

  /**
   * Method that handles the unary RPC. The server sends a single response to the client request
   * acknowledging that the request was received.
   */
  @Override
  public void unaryRpc(RequestMessage request, StreamObserver<ResponseMessage> responseObserver) {
    ResponseMessage response =
        ResponseMessage.newBuilder()
            .setText("I received your message: " + request.getText())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  /**
   * Method that handles the server streaming RPC. The server sends 10 response messages to a single
   * client request. After each request is sent, the server pauses execution for a second.
   */
  @Override
  public void serverStreamingRpc(
      RequestMessage request, StreamObserver<ResponseMessage> responseObserver) {
    for (int i = 1; i <= 10; i++) {
      ResponseMessage response =
          ResponseMessage.newBuilder()
              .setText(i + ". response to your message: " + request.getText())
              .build();
      responseObserver.onNext(response);

      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        // just continue
      }
    }

    responseObserver.onCompleted();
  }

  /**
   * Method that handles the client streaming RPC. The server counts the total length of characters
   * in all request messages by the client. Once the client has completed sending all requests, the
   * server responds with the total character length.
   */
  @Override
  public StreamObserver<RequestMessage> clientStreamingRpc(
      StreamObserver<ResponseMessage> responseObserver) {
    return new StreamObserver<>() {
      int totalCharLength = 0;

      @Override
      public void onNext(RequestMessage request) {
        totalCharLength += request.getText().length();
      }

      @Override
      public void onCompleted() {
        ResponseMessage response =
            ResponseMessage.newBuilder()
                .setText("Total length of all request messages: " + totalCharLength)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }

      @Override
      public void onError(Throwable t) {
        Status status = Status.fromThrowable(t);
        System.out.println("Client streaming RPC failed with status code: " + status.getCode());
      }
    };
  }

  /**
   * Method that handles the bidirectional streaming RPC. The server answers directly on each client
   * request. Once the client has completed sending all requests, the server sends a final message.
   */
  @Override
  public StreamObserver<RequestMessage> bidirectionalStreamingRpc(
      StreamObserver<ResponseMessage> responseObserver) {
    return new StreamObserver<>() {
      @Override
      public void onNext(RequestMessage request) {
        ResponseMessage response =
            ResponseMessage.newBuilder()
                .setText("I received your message: " + request.getText())
                .build();

        responseObserver.onNext(response);
      }

      @Override
      public void onCompleted() {
        ResponseMessage response =
            ResponseMessage.newBuilder().setText("This is the last message").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }

      @Override
      public void onError(Throwable t) {
        Status status = Status.fromThrowable(t);
        System.out.println(
            "Bidirectional streaming RPC failed with status code: " + status.getCode());
      }
    };
  }
}
