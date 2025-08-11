package com.example.hanaro.controller;

import com.example.hanaro.dto.ProductResponse;
import com.example.hanaro.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping("/products/{id}")
  public ProductResponse get(@PathVariable Long id) {
    ProductResponse resp = productService.get(id);
    resp = withAbsoluteImageUrl(resp);
    log.info("[PRODUCT][GET] id={} -> createdAt={}, updatedAt={}", id, resp.getCreatedAt(),
        resp.getUpdatedAt());
    return resp;
  }

  @GetMapping("/products")
  public java.util.List<ProductResponse> listProducts(
      @RequestParam(name = "q", required = false) String q
  ) {
    log.info("[PRODUCT][LIST] q='{}'", q);
    return productService.list(q).stream()
        .map(this::withAbsoluteImageUrl)
        .toList();
  }

  private ProductResponse withAbsoluteImageUrl(ProductResponse resp) {
    if (resp == null) {
      return null;
    }
    String url = resp.getImageUrl();
    if (url == null || url.isBlank() || url.startsWith("http")) {
      return resp;
    }
    String absolute = ServletUriComponentsBuilder.fromCurrentContextPath().path(url).toUriString();
    return ProductResponse.builder()
        .id(resp.getId())
        .name(resp.getName())
        .description(resp.getDescription())
        .price(resp.getPrice())
        .stockQuantity(resp.getStockQuantity())
        .imageUrl(absolute)
        .createdAt(resp.getCreatedAt())
        .updatedAt(resp.getUpdatedAt())
        .build();
  }
}