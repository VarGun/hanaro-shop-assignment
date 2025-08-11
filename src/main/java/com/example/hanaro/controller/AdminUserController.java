package com.example.hanaro.controller;

import com.example.hanaro.dto.UserResponse;
import com.example.hanaro.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;

  // 관리자: 회원 목록 조회 (페이지네이션)
  @GetMapping
  public Page<UserResponse> list(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
    log.info("[ADMIN][USERS][LIST] page={}, size={}, sort={}", pageable.getPageNumber(),
        pageable.getPageSize(), pageable.getSort());
    return adminUserService.list(pageable);
  }

  // 관리자: 회원 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    adminUserService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
