package com.example.hanaro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class CartItemResponse {

  private Long productId;
  private String name;
  private int price;       // 단가
  private int quantity;
  private int totalPrice;  // 총 가격
}