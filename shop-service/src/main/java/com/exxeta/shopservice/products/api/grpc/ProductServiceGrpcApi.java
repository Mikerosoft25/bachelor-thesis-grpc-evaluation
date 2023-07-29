package com.exxeta.shopservice.products.api.grpc;

import com.exxeta.recommendationservice.RecommendationGrpcDto;
import com.exxeta.shopservice.CreateProductRequest;
import com.exxeta.shopservice.DeleteProductRequest;
import com.exxeta.shopservice.GetProductRequest;
import com.exxeta.shopservice.ListRecommendedProductsRequest;
import com.exxeta.shopservice.ProductGrpcDto;
import com.exxeta.shopservice.ProductListGrpcDto;
import com.exxeta.shopservice.ProductRecommendationGrpcDto;
import com.exxeta.shopservice.ProductServiceGrpc.ProductServiceImplBase;
import com.exxeta.shopservice.UpdateProductRequest;
import com.exxeta.shopservice.clients.recommendationservice.grpc.RecommendationServiceGrpcClient;
import com.exxeta.shopservice.clients.userservice.grpc.UserServiceGrpcClient;
import com.exxeta.shopservice.orders.service.OrderService;
import com.exxeta.shopservice.products.entity.Category;
import com.exxeta.shopservice.products.entity.Product;
import com.exxeta.shopservice.products.service.CategoryNotFoundException;
import com.exxeta.shopservice.products.service.ProductNotFoundException;
import com.exxeta.shopservice.products.service.ProductService;
import com.exxeta.userservice.UserGrpcDto;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;

@Profile("!rest")
@Controller
public class ProductServiceGrpcApi extends ProductServiceImplBase {
  private final ProductService productService;
  private final OrderService orderService;

  private final UserServiceGrpcClient userServiceGrpcClient;
  private final RecommendationServiceGrpcClient recommendationServiceGrpcClient;

  public ProductServiceGrpcApi(
      ProductService productService,
      OrderService orderService,
      UserServiceGrpcClient userServiceGrpcClient,
      RecommendationServiceGrpcClient recommendationServiceGrpcClient) {
    this.orderService = orderService;
    this.productService = productService;
    this.userServiceGrpcClient = userServiceGrpcClient;
    this.recommendationServiceGrpcClient = recommendationServiceGrpcClient;
  }

  @Override
  public void createProduct(
      CreateProductRequest request, StreamObserver<ProductGrpcDto> responseObserver) {
    if (this.isInvalidCreateProductRestDto(request)) {
      responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
      return;
    }

    Product createdProduct;
    try {
      createdProduct =
          productService.createProduct(
              request.getName(), request.getCategory(), request.getPrice());
    } catch (CategoryNotFoundException ex) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    ProductGrpcDto productDto = this.mapProductToProductGrpcDto(createdProduct);

    responseObserver.onNext(productDto);
    responseObserver.onCompleted();
  }

  @Override
  public void listProducts(Empty request, StreamObserver<ProductListGrpcDto> responseObserver) {
    List<Product> products = productService.getAllProducts();

    ProductListGrpcDto response =
        ProductListGrpcDto.newBuilder()
            .addAllProducts(products.stream().map(this::mapProductToProductGrpcDto).toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getProduct(
      GetProductRequest request, StreamObserver<ProductGrpcDto> responseObserver) {
    Product product;
    try {
      product = productService.getProduct(request.getProductId());
    } catch (ProductNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    ProductGrpcDto productDto = this.mapProductToProductGrpcDto(product);

    responseObserver.onNext(productDto);
    responseObserver.onCompleted();
  }

  @Override
  public void updateProduct(
      UpdateProductRequest request, StreamObserver<ProductGrpcDto> responseObserver) {
    String updatedName = request.hasName() ? request.getName() : null;
    String updatedCategory = request.hasCategory() ? request.getCategory() : null;
    Float updatedPrice = request.hasPrice() ? request.getPrice() : null;

    Product updatedProduct;
    try {
      updatedProduct =
          productService.updateProduct(
              request.getProductId(), updatedName, updatedCategory, updatedPrice);
    } catch (ProductNotFoundException | CategoryNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    ProductGrpcDto productDto = this.mapProductToProductGrpcDto(updatedProduct);

    responseObserver.onNext(productDto);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteProduct(
      DeleteProductRequest request, StreamObserver<ProductGrpcDto> responseObserver) {
    Product deletedProduct;
    try {
      deletedProduct = productService.deleteProduct(request.getProductId());
    } catch (ProductNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
      return;
    }

    ProductGrpcDto productDto = this.mapProductToProductGrpcDto(deletedProduct);

    responseObserver.onNext(productDto);
    responseObserver.onCompleted();
  }

  @Override
  public void listRecommendedProducts(
      ListRecommendedProductsRequest request,
      StreamObserver<ProductRecommendationGrpcDto> responseObserver) {
    try {
      UserGrpcDto userDto = userServiceGrpcClient.getUser(request.getUserId());

      List<Category> boughtCategories = orderService.getBoughtCategoriesByUser(userDto.getId());
      List<Category> availableCategories = List.of(Category.values());

      RecommendationGrpcDto recommendationDto =
          recommendationServiceGrpcClient.createRecommendation(
              boughtCategories, availableCategories);

      List<Category> recommendedCategories =
          recommendationDto.getRecommendedCategoriesList().stream()
              .map(Category::fromName)
              .toList();
      List<Product> recommendedProducts =
          productService.getAllProductsOfCategories(recommendedCategories);

      ProductRecommendationGrpcDto productRecommendationDto =
          this.mapToProductRecommendationDto(recommendedProducts, userDto);

      responseObserver.onNext(productRecommendationDto);
      responseObserver.onCompleted();
    } catch (Exception ex) {
      ex.printStackTrace();
      responseObserver.onError(
          Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
    }
  }

  /**
   * Maps the {@link Product} object to the generated {@link ProductGrpcDto} object that is used as
   * a response type for gRPC requests.
   *
   * @param product the product that should be mapped to the DTO.
   * @return the mapped {@link ProductGrpcDto} object.
   */
  private ProductGrpcDto mapProductToProductGrpcDto(Product product) {
    return ProductGrpcDto.newBuilder()
        .setId(product.getId())
        .setName(product.getName())
        .setCategory(product.getCategory().getName())
        .setPrice(product.getPrice())
        .build();
  }

  /**
   * Maps a list of {@link Product} objects to a list of {@link ProductGrpcDto} objects by calling
   * the {@link #mapProductToProductGrpcDto} method for each product of the list.
   *
   * @param products a list of products that should be mapped.
   * @return a list containing all mapped {@link ProductGrpcDto} objects.
   */
  private List<ProductGrpcDto> mapProductsToProductGrpcDtos(List<Product> products) {
    return products.stream().map(this::mapProductToProductGrpcDto).toList();
  }

  /**
   * Maps a list of recommended {@link Product} together with a {@link UserGrpcDto} to a {@link
   * ProductRecommendationGrpcDto}
   *
   * @param recommendedProducts the recommended products for the user.
   * @param userDto the user for whom the products are recommended.
   * @return the mapped {@link ProductRecommendationGrpcDto}
   */
  private ProductRecommendationGrpcDto mapToProductRecommendationDto(
      List<Product> recommendedProducts, UserGrpcDto userDto) {
    List<ProductGrpcDto> productGrpcDtos = this.mapProductsToProductGrpcDtos(recommendedProducts);

    return ProductRecommendationGrpcDto.newBuilder()
        .addAllRecommendedProducts(productGrpcDtos)
        .setUser(userDto)
        .build();
  }

  /**
   * Checks if all parameters of the request to create a new product are valid.
   *
   * @param request the {@link CreateProductRequest}
   * @return true if any parameter is invalid, false if everything is valid.
   */
  private boolean isInvalidCreateProductRestDto(CreateProductRequest request) {
    return request.getName().isBlank()
        || request.getCategory().isBlank()
        || request.getPrice() <= 0;
  }
}
