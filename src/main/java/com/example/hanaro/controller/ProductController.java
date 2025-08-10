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
    log.info("[PRODUCT][CREATE][REQ] name='{}', price={}, hasImage={} ", req.getName(),
        req.getPrice(), req.getImage() != null && !req.getImage().isEmpty());
    ProductResponse resp = productService.create(req);
    log.info("[PRODUCT][CREATE][RES] id={}, createdAt={}, updatedAt={}", resp.getId(),
        resp.getCreatedAt(), resp.getUpdatedAt());
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  @GetMapping("/products/{id}")
  public ProductResponse get(@PathVariable Long id) {
    ProductResponse resp = productService.get(id);
    log.info("[PRODUCT][GET] id={} -> createdAt={}, updatedAt={}", id, resp.getCreatedAt(),
        resp.getUpdatedAt());
    return resp;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/admin/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ProductResponse update(@PathVariable Long id,
      @Validated @ModelAttribute ProductCreateRequest req) {
    log.info("[PRODUCT][UPDATE][REQ] id={}, name='{}', price={}, hasImage={}", id, req.getName(),
        req.getPrice(), req.getImage() != null && !req.getImage().isEmpty());
    ProductResponse resp = productService.update(id, req);
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
    return productService.list(q);
  }
}