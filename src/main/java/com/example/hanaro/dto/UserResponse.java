package com.example.hanaro.dto;

import com.example.hanaro.entity.User;
import lombok.Builder;

@Builder
public record UserResponse(
    Long id,
    String email,
    String name,
    String phone
) {

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .phone(user.getPhone())
        .build();
  }
}