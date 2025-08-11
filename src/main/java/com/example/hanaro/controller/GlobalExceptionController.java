package com.example.hanaro.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getAllErrors().stream()
        .findFirst()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .orElse("Validation failed");
    return Map.of("error", "VALIDATION", "message", msg);
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, Object> handleNotFound(NoSuchElementException e) {
    return Map.of("error", "NOT_FOUND", "message", e.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleIllegalState(IllegalStateException e) {
    return Map.of("error", "CONFLICT", "message", e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleIllegalArgument(IllegalArgumentException e) {
    return Map.of("error", "BAD_REQUEST", "message", e.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleMaxUpload(MaxUploadSizeExceededException e,
      HttpServletRequest request) {
    log.info("[UPLOAD][MAX] uri={}, method={}, contentLength={}, contentType={}",
        request.getRequestURI(), request.getMethod(), request.getContentLengthLong(),
        request.getContentType());
    return Map.of(
        "error", "BAD_REQUEST",
        "message", "업로드 최대 용량(총 3MB)을 초과했습니다."
    );
  }

  @ExceptionHandler(MultipartException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleMultipart(MultipartException e, HttpServletRequest request) {
    log.info("[UPLOAD][MULTIPART] uri={}, method={}, contentLength={}, contentType={}",
        request.getRequestURI(), request.getMethod(), request.getContentLengthLong(),
        request.getContentType());
    Throwable root = e.getCause();
    if (root != null) {
      log.info("MultipartException root cause: {}: {}", root.getClass().getName(),
          root.getMessage());
    } else {
      log.info("MultipartException root cause: null");
    }
    if (root instanceof FileSizeLimitExceededException) {
      FileSizeLimitExceededException ex = (FileSizeLimitExceededException) root;
      log.info("File size exceeded: actual={}, permitted={}", ex.getActualSize(),
          ex.getPermittedSize());
      return Map.of(
          "error", "BAD_REQUEST",
          "message", "이미지 크기는 1개당 512KB 이하여야 합니다."
      );
    }
    if (root instanceof SizeLimitExceededException) {
      SizeLimitExceededException ex = (SizeLimitExceededException) root;
      log.info("File size exceeded: actual={}, permitted={}", ex.getActualSize(),
          ex.getPermittedSize());
      return Map.of(
          "error", "BAD_REQUEST",
          "message", "업로드 최대 용량(총 3MB)을 초과했습니다."
      );
    }
    return Map.of(
        "error", "BAD_REQUEST",
        "message", "멀티파트 요청 처리 중 오류가 발생했습니다."
    );
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public Map<String, Object> handleBadCredentials(BadCredentialsException e) {
    return Map.of("error", "UNAUTHORIZED", "message", "이메일 또는 비밀번호가 올바르지 않습니다.");
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public Map<String, Object> handleAuth(AuthenticationException e) {
    return Map.of("error", "UNAUTHORIZED", "message", "인증이 필요합니다.");
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Map<String, Object> handleAccess(AccessDeniedException e) {
    return Map.of("error", "FORBIDDEN", "message", "접근 권한이 없습니다.");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return Map.of("error", "BAD_REQUEST", "message", "요청 파라미터 타입이 올바르지 않습니다.");
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleNotReadable(HttpMessageNotReadableException e) {
    return Map.of("error", "BAD_REQUEST", "message", "요청 본문을 읽을 수 없습니다.");
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleConstraint(ConstraintViolationException e) {
    String msg = e.getConstraintViolations().stream().findFirst()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .orElse("요청 값이 올바르지 않습니다.");
    return Map.of("error", "BAD_REQUEST", "message", msg);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleDataConflict(DataIntegrityViolationException e) {
    return Map.of("error", "CONFLICT", "message", "데이터 제약 조건을 위반했습니다.");
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String, Object> handleEtc(Exception e) {
    log.error("[UNHANDLED] {}: {}", e.getClass().getName(), e.getMessage(), e);
    return Map.of("error", "INTERNAL_SERVER_ERROR", "message", "서버 오류가 발생했습니다.");
  }
}