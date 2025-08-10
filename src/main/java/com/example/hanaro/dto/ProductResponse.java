package com.example.hanaro.dto;

import com.example.hanaro.entity.Product;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

  private Long id;
  private String name;
  private String description;
  private int price;
  private int stockQuantity;
  private String imageUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static ProductResponse from(Product product) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .stockQuantity(product.getStockQuantity())
        .imageUrl(product.getImageUrl())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
}