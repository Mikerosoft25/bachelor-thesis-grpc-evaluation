package com.exxeta.shopservice.products.api.rest;

import com.exxeta.shopservice.clients.recommendationservice.rest.RecommendationServiceRestClient;
import com.exxeta.shopservice.clients.recommendationservice.rest.dto.RecommendationRestDto;
import com.exxeta.shopservice.clients.userservice.rest.UserServiceRestClient;
import com.exxeta.shopservice.clients.userservice.rest.dto.UserRestDto;
import com.exxeta.shopservice.orders.service.OrderService;
import com.exxeta.shopservice.products.api.rest.dto.CreateProductRestDto;
import com.exxeta.shopservice.products.api.rest.dto.ProductRecommendationRestDto;
import com.exxeta.shopservice.products.api.rest.dto.ProductRestDto;
import com.exxeta.shopservice.products.api.rest.dto.UpdateProductRestDto;
import com.exxeta.shopservice.products.entity.Category;
import com.exxeta.shopservice.products.entity.Product;
import com.exxeta.shopservice.products.service.CategoryNotFoundException;
import com.exxeta.shopservice.products.service.ProductNotFoundException;
import com.exxeta.shopservice.products.service.ProductService;
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
@RequestMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductServiceRestApi {
  private final ProductService productService;
  private final OrderService orderService;

  private final UserServiceRestClient userServiceRestClient;
  private final RecommendationServiceRestClient recommendationServiceRestClient;

  public ProductServiceRestApi(
      ProductService productService,
      UserServiceRestClient userServiceRestClient,
      RecommendationServiceRestClient recommendationServiceRestClient,
      OrderService orderService) {
    this.productService = productService;
    this.orderService = orderService;

    this.userServiceRestClient = userServiceRestClient;
    this.recommendationServiceRestClient = recommendationServiceRestClient;
  }

  @PostMapping
  public ResponseEntity<ProductRestDto> createProduct(
      @RequestBody CreateProductRestDto createProductDto) {
    if (this.isInvalidCreateProductRestDto(createProductDto)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    Product createdProduct;
    try {
      createdProduct =
          productService.createProduct(
              createProductDto.name(), createProductDto.category(), createProductDto.price());
    } catch (CategoryNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    ProductRestDto productDto = this.mapProductToProductRestDto(createdProduct);

    return ResponseEntity.status(HttpStatus.CREATED).body(productDto);
  }

  @GetMapping
  public ResponseEntity<List<ProductRestDto>> listProducts() {
    List<Product> products = productService.getAllProducts();

    List<ProductRestDto> productDtos = this.mapProductListToProductRestDtoList(products);
    return ResponseEntity.ok(productDtos);
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductRestDto> getProduct(@PathVariable Integer productId) {
    Product product;
    try {
      product = productService.getProduct(productId);
    } catch (ProductNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    ProductRestDto productDto = this.mapProductToProductRestDto(product);
    return ResponseEntity.ok(productDto);
  }

  @PatchMapping("/{productId}")
  public ResponseEntity<ProductRestDto> updateProduct(
      @PathVariable Integer productId, @RequestBody UpdateProductRestDto updateProductDto) {
    Product updatedProduct;
    try {
      updatedProduct =
          productService.updateProduct(
              productId,
              updateProductDto.name(),
              updateProductDto.category(),
              updateProductDto.price());
    } catch (ProductNotFoundException | CategoryNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    ProductRestDto productDto = this.mapProductToProductRestDto(updatedProduct);
    return ResponseEntity.ok(productDto);
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<ProductRestDto> deleteProduct(@PathVariable Integer productId) {
    Product deletedProduct;
    try {
      deletedProduct = productService.deleteProduct(productId);
    } catch (ProductNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    ProductRestDto productDto = this.mapProductToProductRestDto(deletedProduct);
    return ResponseEntity.ok(productDto);
  }

  @GetMapping("/recommended/{userId}")
  public ResponseEntity<ProductRecommendationRestDto> listRecommendedProducts(
      @PathVariable Integer userId) {
    try {
      UserRestDto userDto = userServiceRestClient.getUser(userId);

      List<Category> boughtCategories = orderService.getBoughtCategoriesByUser(userDto.id());
      List<Category> availableCategories = List.of(Category.values());

      RecommendationRestDto recommendationDto =
          recommendationServiceRestClient.getRecommendedCategories(
              boughtCategories, availableCategories);

      List<Category> recommendedCategories =
          recommendationDto.recommendedCategories().stream().map(Category::fromName).toList();
      List<Product> recommendedProducts =
          productService.getAllProductsOfCategories(recommendedCategories);

      ProductRecommendationRestDto productRecommendationDto =
          this.mapToProductRecommendationDto(recommendedProducts, userDto);

      return ResponseEntity.ok(productRecommendationDto);
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

  /**
   * Maps a {@link Product} to a {@link ProductRestDto}.
   *
   * @param product the product that should be mapped.
   * @return the mapped {@link ProductRestDto}.
   */
  private ProductRestDto mapProductToProductRestDto(Product product) {
    return new ProductRestDto(
        product.getId(), product.getName(), product.getPrice(), product.getCategory().getName());
  }

  /**
   * Maps a list of {@link Product} to a list of {@link ProductRestDto}.
   *
   * @param products the products that should be mapped.
   * @return a list containing all mapped {@link ProductRestDto}.
   */
  private List<ProductRestDto> mapProductListToProductRestDtoList(List<Product> products) {
    return products.stream().map(this::mapProductToProductRestDto).toList();
  }

  /**
   * Maps a list of recommended {@link Product} together with a {@link UserRestDto} to a {@link
   * ProductRecommendationRestDto}
   *
   * @param recommendedProducts the recommended products for the user.
   * @param userDto the user for whom the products are recommended.
   * @return the mapped {@link ProductRecommendationRestDto}
   */
  private ProductRecommendationRestDto mapToProductRecommendationDto(
      List<Product> recommendedProducts, UserRestDto userDto) {
    List<ProductRestDto> productDtoList =
        this.mapProductListToProductRestDtoList(recommendedProducts);

    return new ProductRecommendationRestDto(userDto, productDtoList);
  }

  /**
   * Checks if all parameters of the request to create a new product are valid.
   *
   * @param request the {@link CreateProductRestDto}
   * @return true if any parameter is invalid, false if everything is valid.
   */
  private boolean isInvalidCreateProductRestDto(CreateProductRestDto request) {
    return request.name().isBlank() || request.category().isBlank() || request.price() <= 0;
  }
}
