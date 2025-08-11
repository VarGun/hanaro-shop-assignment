// com.example.hanaro.controller.UserController
package com.example.hanaro.controller;

import com.example.hanaro.dto.UserResponse;
import com.example.hanaro.dto.UserUpdateRequest;
import com.example.hanaro.security.CustomUserDetails;
import com.example.hanaro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  // 본인 or ADMIN만 수정 가능
  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<UserResponse> update(
      @PathVariable Long id,
      @RequestBody @Valid UserUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    return ResponseEntity.ok(userService.updateProfile(id, request, principal));
  }
}