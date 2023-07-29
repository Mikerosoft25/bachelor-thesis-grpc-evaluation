package com.exxeta.shopservice.orders.api.grpc;

import com.exxeta.shopservice.CreateOrderRequest;
import com.exxeta.shopservice.DeleteOrderRequest;
import com.exxeta.shopservice.GetOrderRequest;
import com.exxeta.shopservice.OrderGrpcDto;
import com.exxeta.shopservice.OrderListGrpcDto;
import com.exxeta.shopservice.OrderServiceGrpc.OrderServiceImplBase;
import com.exxeta.shopservice.ProductGrpcDto;
import com.exxeta.shopservice.ProductList;
import com.exxeta.shopservice.UpdateOrderRequest;
import com.exxeta.shopservice.clients.userservice.grpc.UserServiceGrpcClient;
import com.exxeta.shopservice.orders.entity.Order;
import com.exxeta.shopservice.orders.service.OrderNotFoundException;
import com.exxeta.shopservice.orders.service.OrderService;
import com.exxeta.shopservice.products.entity.Product;
import com.exxeta.userservice.CreateUserRequest;
import com.exxeta.userservice.UserGrpcDto;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;

@Profile("!rest")
@Controller
public class OrderServiceGrpcApi extends OrderServiceImplBase {
  private final OrderService orderService;
  private final UserServiceGrpcClient userServiceGrpcClient;

  public OrderServiceGrpcApi(
      OrderService orderService, UserServiceGrpcClient userServiceGrpcClient) {
    this.orderService = orderService;
    this.userServiceGrpcClient = userServiceGrpcClient;
  }

  @Override
  public void createOrder(
      CreateOrderRequest request, StreamObserver<OrderGrpcDto> responseObserver) {
    if (this.isInvalidCreateOrderRequest(request)) {
      responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
      return;
    }

    UserGrpcDto userDto;
    try {
      userDto = userServiceGrpcClient.getUser(request.getUserId());
    } catch (Exception ex) {
      responseObserver.onError(Status.INTERNAL.asRuntimeException());
      return;
    }

    Order order = this.orderService.createOrder(userDto.getId(), request.getProductIdsList());
    OrderGrpcDto orderDto = this.mapOrderToOrderGrpcDto(order, userDto);

    responseObserver.onNext(orderDto);
    responseObserver.onCompleted();
  }

  @Override
  public void listOrders(Empty request, StreamObserver<OrderListGrpcDto> responseObserver) {
    List<Order> orders = orderService.listOrders();

    List<Integer> userIds = orders.stream().map(Order::getUserId).toList();
    List<UserGrpcDto> userDtos = userServiceGrpcClient.listUsers(userIds);

    List<OrderGrpcDto> orderDtos = new ArrayList<>();
    for (Order order : orders) {
      for (UserGrpcDto userDto : userDtos) {
        if (userDto.getId() == order.getUserId()) {
          OrderGrpcDto orderDto = this.mapOrderToOrderGrpcDto(order, userDto);
          orderDtos.add(orderDto);
          break;
        }
      }
    }

    OrderListGrpcDto response = OrderListGrpcDto.newBuilder().addAllOrders(orderDtos).build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getOrder(GetOrderRequest request, StreamObserver<OrderGrpcDto> responseObserver) {
    Order order;
    UserGrpcDto userDto;
    try {
      order = orderService.getOrder(request.getOrderId());
      userDto = userServiceGrpcClient.getUser(order.getUserId());
    } catch (OrderNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (Exception ex) {
      responseObserver.onError(Status.INTERNAL.asRuntimeException());
      return;
    }

    OrderGrpcDto orderDto = this.mapOrderToOrderGrpcDto(order, userDto);
    responseObserver.onNext(orderDto);
    responseObserver.onCompleted();
  }

  @Override
  public void updateOrder(
      UpdateOrderRequest request, StreamObserver<OrderGrpcDto> responseObserver) {
    Order updatedOrder;
    UserGrpcDto userDto;
    try {
      updatedOrder = orderService.updateOrder(request.getOrderId(), request.getProductIdsList());
      userDto = userServiceGrpcClient.getUser(updatedOrder.getUserId());
    } catch (OrderNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (Exception ex) {
      responseObserver.onError(Status.INTERNAL.asRuntimeException());
      return;
    }

    OrderGrpcDto orderDto = this.mapOrderToOrderGrpcDto(updatedOrder, userDto);

    responseObserver.onNext(orderDto);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteOrder(
      DeleteOrderRequest request, StreamObserver<OrderGrpcDto> responseObserver) {
    Order deletedOrder;
    UserGrpcDto userDto;
    try {
      deletedOrder = orderService.deleteOrder(request.getOrderId());
      userDto = userServiceGrpcClient.getUser(deletedOrder.getUserId());
    } catch (OrderNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    } catch (Exception ex) {
      responseObserver.onError(Status.INTERNAL.asRuntimeException());
      return;
    }

    OrderGrpcDto orderDto = this.mapOrderToOrderGrpcDto(deletedOrder, userDto);

    responseObserver.onNext(orderDto);
    responseObserver.onCompleted();
  }

  /**
   * Maps the {@link Order} object together with the corresponding {@link UserGrpcDto} to the
   * generated {@link OrderGrpcDto} object that is used as a response type for gRPC requests.
   *
   * @param order the order that should be mapped to the DTO.
   * @param userDto the user that created the order.
   * @return the mapped {@link UserGrpcDto} object.
   */
  private OrderGrpcDto mapOrderToOrderGrpcDto(Order order, UserGrpcDto userDto) {
    var productListBuilder = ProductList.newBuilder();
    for (Product product : order.getProducts()) {
      productListBuilder.addProducts(
          ProductGrpcDto.newBuilder()
              .setId(product.getId())
              .setName(product.getName())
              .setCategory(product.getCategory().getName())
              .setPrice(product.getPrice())
              .build());
    }

    String isoTimestamp = DateTimeFormatter.ISO_DATE_TIME.format(order.getDateTime());
    return OrderGrpcDto.newBuilder()
        .setId(order.getId())
        .setIsoTimestamp(isoTimestamp)
        .setProducts(productListBuilder.build())
        .setTotalPrice(order.getTotalPrice())
        .setUser(userDto)
        .build();
  }

  /**
   * Checks if all parameters of the request to create a new order are valid.
   *
   * @param request the {@link CreateUserRequest}
   * @return true if any parameter is invalid, false if everything is valid.
   */
  private boolean isInvalidCreateOrderRequest(CreateOrderRequest request) {
    return request.getUserId() <= 0 || request.getProductIdsList().isEmpty();
  }
}
