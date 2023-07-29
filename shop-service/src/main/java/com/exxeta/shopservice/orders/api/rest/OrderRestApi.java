package com.exxeta.shopservice.orders.api.rest;

import com.exxeta.shopservice.clients.userservice.rest.UserServiceRestClient;
import com.exxeta.shopservice.clients.userservice.rest.dto.UserRestDto;
import com.exxeta.shopservice.orders.api.rest.dto.CreateOrderRestDto;
import com.exxeta.shopservice.orders.api.rest.dto.OrderRestDto;
import com.exxeta.shopservice.orders.api.rest.dto.UpdateOrderRestDto;
import com.exxeta.shopservice.orders.entity.Order;
import com.exxeta.shopservice.orders.service.OrderNotFoundException;
import com.exxeta.shopservice.orders.service.OrderService;
import com.exxeta.shopservice.products.api.rest.dto.ProductRestDto;
import com.exxeta.shopservice.products.service.ProductNotFoundException;
import com.exxeta.userservice.CreateUserRequest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Profile("!grpc")
@RestController
@RequestMapping(path = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderRestApi {

  public OrderService orderService;
  public UserServiceRestClient userServiceRestClient;

  public OrderRestApi(OrderService orderService, UserServiceRestClient userServiceRestClient) {
    this.orderService = orderService;
    this.userServiceRestClient = userServiceRestClient;
  }

  @PostMapping()
  public ResponseEntity<OrderRestDto> createOrder(@RequestBody CreateOrderRestDto createOrderDto) {
    if (this.isInvalidCreateOrderRequest(createOrderDto)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    UserRestDto userDto;
    try {
      userDto = userServiceRestClient.getUser(createOrderDto.userId());
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    Order order = orderService.createOrder(userDto.id(), createOrderDto.productIds());

    OrderRestDto orderDto = this.mapOrderToOrderDto(order, userDto);
    return ResponseEntity.ok(orderDto);
  }

  @GetMapping
  public ResponseEntity<List<OrderRestDto>> listOrders() {

    List<Order> orders = orderService.listOrders();

    List<UserRestDto> userDtos;
    try {
      userDtos = userServiceRestClient.listUsers(orders.stream().map(Order::getUserId).toList());
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    List<OrderRestDto> orderDtos = new ArrayList<>();
    for (Order order : orders) {
      for (UserRestDto userDto : userDtos) {
        if (userDto.id().equals(order.getUserId())) {
          OrderRestDto orderDto = this.mapOrderToOrderDto(order, userDto);
          orderDtos.add(orderDto);
          break;
        }
      }
    }

    return ResponseEntity.ok(orderDtos);
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderRestDto> getOrder(@PathVariable Integer orderId) {
    Order order;
    UserRestDto userDto;
    try {
      order = orderService.getOrder(orderId);
      userDto = userServiceRestClient.getUser(order.getUserId());
    } catch (OrderNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    OrderRestDto orderDto = this.mapOrderToOrderDto(order, userDto);
    return ResponseEntity.ok(orderDto);
  }

  @PatchMapping("/{orderId}")
  public ResponseEntity<OrderRestDto> updateOrder(
      @PathVariable Integer orderId, @RequestBody UpdateOrderRestDto updateOrderDto) {
    Order updatedOrder;
    UserRestDto userDto;
    try {
      updatedOrder = orderService.updateOrder(orderId, updateOrderDto.productIds());
      userDto = userServiceRestClient.getUser(updatedOrder.getUserId());
    } catch (OrderNotFoundException | ProductNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    OrderRestDto productDto = this.mapOrderToOrderDto(updatedOrder, userDto);
    return ResponseEntity.ok(productDto);
  }

  @DeleteMapping("/{orderId}")
  public ResponseEntity<OrderRestDto> deleteOrder(@PathVariable Integer orderId) {
    Order deletedOrder;
    UserRestDto userDto;
    try {
      deletedOrder = orderService.deleteOrder(orderId);
      userDto = userServiceRestClient.getUser(orderId);
    } catch (OrderNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    OrderRestDto productDto = this.mapOrderToOrderDto(deletedOrder, userDto);
    return ResponseEntity.ok(productDto);
  }

  /**
   * Maps the {@link Order} object together with the corresponding {@link UserRestDto} to the {@link
   * OrderRestDto} object that is used as a response type for REST requests.
   *
   * @param order the order that should be mapped to the DTO.
   * @param userDto the user that created the order.
   * @return the mapped {@link OrderRestDto} object.
   */
  private OrderRestDto mapOrderToOrderDto(Order order, UserRestDto userDto) {
    List<ProductRestDto> productDtos =
        order.getProducts().stream()
            .map(
                product ->
                    new ProductRestDto(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getCategory().getName()))
            .toList();

    String isoTimestamp = DateTimeFormatter.ISO_DATE_TIME.format(order.getDateTime());
    return new OrderRestDto(
        order.getId(), isoTimestamp, productDtos, order.getTotalPrice(), userDto);
  }

  /**
   * Checks if all parameters of the request to create a new order are valid.
   *
   * @param request the {@link CreateUserRequest}
   * @return true if any parameter is invalid, false if everything is valid.
   */
  private boolean isInvalidCreateOrderRequest(CreateOrderRestDto request) {
    return request.userId() <= 0 || request.productIds().isEmpty();
  }
}
