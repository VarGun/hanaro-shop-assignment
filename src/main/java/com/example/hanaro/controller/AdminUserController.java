package com.example.hanaro.controller;

import com.example.hanaro.dto.UserResponse;
import com.example.hanaro.service.AdminUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;
  
  // 관리자: 회원 조회 (검색)
  @GetMapping
  public List<UserResponse> search(@RequestParam(required = false, name = "q") String q) {
    return adminUserService.search(q);
  }

  // 관리자: 회원 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    adminUserService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
