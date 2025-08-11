package com.example.hanaro.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

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
    System.out.println("[UPLOAD][MAX] uri=" + request.getRequestURI()
        + ", method=" + request.getMethod()
        + ", contentLength=" + request.getContentLengthLong()
        + ", contentType=" + request.getContentType());
    return Map.of(
        "error", "BAD_REQUEST",
        "message", "업로드 최대 용량(총 3MB)을 초과했습니다."
    );
  }

  @ExceptionHandler(MultipartException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleMultipart(MultipartException e, HttpServletRequest request) {
    System.out.println("[UPLOAD][MULTIPART] uri=" + request.getRequestURI()
        + ", method=" + request.getMethod()
        + ", contentLength=" + request.getContentLengthLong()
        + ", contentType=" + request.getContentType());
    Throwable root = e.getCause();
    if (root != null) {
      System.out.println(
          "MultipartException root cause: " + root.getClass().getName() + ": " + root.getMessage());
    } else {
      System.out.println("MultipartException root cause: null");
    }
    if (root instanceof FileSizeLimitExceededException) {
      FileSizeLimitExceededException ex = (FileSizeLimitExceededException) root;
      System.out.println("File size exceeded: actual=" + ex.getActualSize() + ", permitted="
          + ex.getPermittedSize());
      return Map.of(
          "error", "BAD_REQUEST",
          "message", "이미지 크기는 1개당 512KB 이하여야 합니다."
      );
    }
    if (root instanceof SizeLimitExceededException) {
      SizeLimitExceededException ex = (SizeLimitExceededException) root;
      System.out.println("File size exceeded: actual=" + ex.getActualSize() + ", permitted="
          + ex.getPermittedSize());
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
}