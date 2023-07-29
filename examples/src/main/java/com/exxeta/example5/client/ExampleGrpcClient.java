package com.exxeta.example5.client;

import com.exxeta.protos.example5.ExampleServiceGrpc;
import com.exxeta.protos.example5.ExampleServiceGrpc.ExampleServiceBlockingStub;
import com.exxeta.protos.example5.ExampleServiceGrpc.ExampleServiceStub;
import com.exxeta.protos.example5.RequestMessage;
import com.exxeta.protos.example5.ResponseMessage;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Implementation of a gRPC client that calls all four types of RPC */
public class ExampleGrpcClient {

  private final ExampleServiceBlockingStub blockingStub;
  private final ExampleServiceStub asyncStub;

  public static void main(String[] args) throws InterruptedException {
    ExampleGrpcClient client = new ExampleGrpcClient();

    System.out.println("=================== Unary RPC ===================");
    client.executeUnaryRpc();
    System.out.println("\n============= Client Streaming RPC ==============");
    client.executeClientStreamingRpc();
    System.out.println("\n============= Server Streaming RPC ==============");
    client.executeServerStreamingRpc();
    System.out.println("\n========== Bidirectional Streaming RPC ==========");
    client.executeBidirectionalRpc();
  }

  public ExampleGrpcClient() {
    Channel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();

    this.blockingStub = ExampleServiceGrpc.newBlockingStub(channel);
    this.asyncStub = ExampleServiceGrpc.newStub(channel);
  }

  /** Executes a synchronous unary RPC. Prints the received response to the console. */
  public void executeUnaryRpc() {
    RequestMessage requestMessage = RequestMessage.newBuilder().setText("Request text").build();
    ResponseMessage response = blockingStub.unaryRpc(requestMessage);

    System.out.println("Received unary RPC response: " + response.getText());
    System.out.println("Unary RPC finished");
  }

  /** Executes a synchronous server streaming RPC. Prints all received responses to the console. */
  public void executeServerStreamingRpc() {
    RequestMessage request = RequestMessage.newBuilder().setText("Request text").build();
    System.out.println("Sending server streaming RPC request to the server: " + request.getText());
    Iterator<ResponseMessage> responses = blockingStub.serverStreamingRpc(request);

    while (responses.hasNext()) {
      ResponseMessage response = responses.next();
      System.out.println("Received server streaming RPC response: " + response.getText());
    }

    System.out.println("Server streaming RPC finished");
  }

  /**
   * Executes an asynchronous client streaming RPC. Prints all requests and the received response to
   * the console. The client waits one seconds after sending each request. For the sake of this
   * example, a CountDownLatch is used to await the completion of the RPC.
   */
  public void executeClientStreamingRpc() throws InterruptedException {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    StreamObserver<ResponseMessage> responseObserver =
        new StreamObserver<>() {
          @Override
          public void onNext(ResponseMessage response) {
            System.out.println("Received client streaming RPC response: " + response.getText());
          }

          @Override
          public void onCompleted() {
            System.out.println("Client streaming RPC finished on the server-side");
            finishLatch.countDown();
          }

          @Override
          public void onError(Throwable t) {
            Status status = Status.fromThrowable(t);
            System.out.println("Client streaming RPC failed with status code: " + status.getCode());
            finishLatch.countDown();
          }
        };

    StreamObserver<RequestMessage> requestObserver = asyncStub.clientStreamingRpc(responseObserver);
    for (int i = 1; i <= 10; i++) {
      RequestMessage request =
          RequestMessage.newBuilder().setText(i + ". request to the server").build();
      System.out.println(
          "Sending client streaming RPC request to the server: " + request.getText());
      requestObserver.onNext(request);

      Thread.sleep(1000);
    }

    requestObserver.onCompleted();

    // Await until the RPC is finished since responses are received asynchronously
    finishLatch.await(1, TimeUnit.MINUTES);
    System.out.println("Client streaming RPC finished");
  }

  /**
   * Executes an asynchronous bidirectional streaming RPC. Prints all requests and responses to the
   * console. The client waits one seconds after sending each request. For the sake of this example,
   * a CountDownLatch is used to await the completion of the RPC.
   */
  public void executeBidirectionalRpc() throws InterruptedException {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    StreamObserver<ResponseMessage> responseObserver =
        new StreamObserver<>() {
          @Override
          public void onNext(ResponseMessage request) {
            System.out.println(
                "Received bidirectional streaming RPC response: " + request.getText());
          }

          @Override
          public void onCompleted() {
            System.out.println("Bidirectional streaming RPC finished on the server-side");
            finishLatch.countDown();
          }

          @Override
          public void onError(Throwable t) {
            Status status = Status.fromThrowable(t);
            System.out.println(
                "Bidirectional streaming RPC failed with status code: " + status.getCode());
            finishLatch.countDown();
          }
        };

    StreamObserver<RequestMessage> requestObserver =
        this.asyncStub.bidirectionalStreamingRpc(responseObserver);

    for (int i = 1; i <= 10; i++) {
      RequestMessage request =
          RequestMessage.newBuilder().setText(i + ". request to the server").build();
      System.out.println(
          "Sending bidirectional streaming request to the server: " + request.getText());
      requestObserver.onNext(request);

      Thread.sleep(1000);
    }

    requestObserver.onCompleted();

    // Await until the RPC is finished since responses are received asynchronously
    finishLatch.await(1, TimeUnit.MINUTES);
    System.out.println("Bidirectional streaming RPC finished");
  }
}
