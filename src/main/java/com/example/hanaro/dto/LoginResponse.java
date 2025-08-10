package com.example.hanaro.dto;

import com.example.hanaro.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

  private Long userId;
  private String email;
  private String name;
  private Role role;
}