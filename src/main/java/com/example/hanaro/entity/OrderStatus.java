package com.example.hanaro.entity;

public enum OrderStatus {
  ORDERED, // 주문완료
  READY, // 배송준비
  SHIPPING, // 배송중
  COMPLETED, // 배송완료
  CANCELED // 취소
}