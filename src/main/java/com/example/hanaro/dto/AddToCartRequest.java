package com.example.hanaro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddToCartRequest {

  @NotNull(message = "상품 ID는 필수입니다.")
  private Long productId;

  @NotNull(message = "수량은 필수입니다.")
  @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
  private Integer quantity;

  @Builder
  public AddToCartRequest(Long productId, int quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }
}