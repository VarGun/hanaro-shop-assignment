package com.example.hanaro.controller;

import com.example.hanaro.dto.ProductCreateRequest;
import com.example.hanaro.dto.ProductResponse;
import com.example.hanaro.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(value = "/admin/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ProductResponse> create(
      @Validated @ModelAttribute ProductCreateRequest req
  ) {
    if (req.getImage() != null && !req.getImage().isEmpty()) {
      long totalSize = req.getImage().getSize();
      if (totalSize > 3 * 1024 * 1024) {
        throw new IllegalArgumentException("업로드 최대 용량(총 3MB)을 초과했습니다. 222");
      }
    }
    log.info("[PRODUCT][CREATE][REQ] name='{}', price={}, hasImage={} ", req.getName(),
        req.getPrice(), req.getImage() != null && !req.getImage().isEmpty());
    ProductResponse resp = productService.create(req);
    resp = withAbsoluteImageUrl(resp);
    log.info("[PRODUCT][CREATE][RES] id={}, createdAt={}, updatedAt={}", resp.getId(),
        resp.getCreatedAt(), resp.getUpdatedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  @GetMapping("/products/{id}")
  public ProductResponse get(@PathVariable Long id) {
    ProductResponse resp = productService.get(id);
    resp = withAbsoluteImageUrl(resp);
    log.info("[PRODUCT][GET] id={} -> createdAt={}, updatedAt={}", id, resp.getCreatedAt(),
        resp.getUpdatedAt());
    return resp;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/admin/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ProductResponse update(@PathVariable Long id,
      @Validated @ModelAttribute ProductCreateRequest req) {
    if (req.getImage() != null && !req.getImage().isEmpty()) {
      long totalSize = req.getImage().getSize();
      if (totalSize > 3 * 1024 * 1024) {
        throw new IllegalArgumentException("업로드 최대 용량(총 3MB)을 초과했습니다.");
      }
    }
    log.info("[PRODUCT][UPDATE][REQ] id={}, name='{}', price={}, hasImage={}", id, req.getName(),
        req.getPrice(), req.getImage() != null && !req.getImage().isEmpty());
    ProductResponse resp = productService.update(id, req);
    resp = withAbsoluteImageUrl(resp);
    log.info("[PRODUCT][UPDATE][RES] id={}, createdAt={}, updatedAt={}", resp.getId(),
        resp.getCreatedAt(), resp.getUpdatedAt());
    return resp;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/admin/products/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    productService.delete(id);
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