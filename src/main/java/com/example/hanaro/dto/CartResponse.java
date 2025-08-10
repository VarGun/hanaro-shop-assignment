package com.example.hanaro.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartResponse {

  private final List<CartItemResponse> items;
  private final int cartTotal;
}