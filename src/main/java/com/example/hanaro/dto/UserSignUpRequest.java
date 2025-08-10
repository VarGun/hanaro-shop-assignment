package com.example.hanaro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignUpRequest {


  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
  private String password;

  @NotBlank(message = "이름은 필수입니다.")
  @Size(max = 50, message = "이름은 최대 50자까지 가능합니다.")
  private String name;
  
  @Size(max = 20, message = "전화번호는 최대 20자까지 가능합니다.")
  private String phone;

  @Builder
  public UserSignUpRequest(String email, String password, String name, String phone) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.phone = phone;
  }
}