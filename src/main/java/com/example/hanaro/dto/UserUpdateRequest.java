// com.example.hanaro.dto.UserUpdateRequest
package com.example.hanaro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Email String email,
    @Size(min = 2, max = 20) String name,
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "휴대폰 형식: 010-0000-0000")
    String phone,
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자")
    String password
) {

}