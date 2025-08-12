// service/storage/FileStorageService.java
package com.example.hanaro.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Service
public class FileStorageService {

  @Value("${app.file.origin-root:src/main/resources/static/origin}")
  private String originRoot;   // 원본 저장 위치

  @Value("${app.file.upload-root:src/main/resources/static/upload}")
  private String uploadRoot;   // 업로드(서빙) 위치

  private static final long MAX_IMAGE_BYTES = 512 * 1024; // 512KB
  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

  public String save(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }

    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new IllegalArgumentException("이미지 형식은 jpg 또는 png만 허용됩니다.");
    }
    if (file.getSize() > MAX_IMAGE_BYTES) {
      throw new IllegalArgumentException("이미지 크기는 512KB 이하여야 합니다.");
    }

    // 2) 날짜별 디렉토리(yyyy/MM/dd)
    LocalDate today = LocalDate.now();
    String yyyy = String.valueOf(today.getYear());
    String MM = String.format("%02d", today.getMonthValue());
    String dd = String.format("%02d", today.getDayOfMonth());

    // 3) 파일명: UUID + 확장자(content-type 기준)
    String ext = "image/png".equals(contentType) ? ".png" : ".jpg";
    String filename = UUID.randomUUID() + ext;

    try {
      // 4) 원본 저장: {originRoot}/{filename}
      Path originDir = Paths.get(originRoot).toAbsolutePath();
      Files.createDirectories(originDir);
      Path originPath = originDir.resolve(filename);
      file.transferTo(originPath.toFile());

      // 5) 업로드 저장: {uploadRoot}/yyyy/MM/dd/{filename}
      Path uploadDir = Paths.get(uploadRoot, yyyy, MM, dd).toAbsolutePath();
      Files.createDirectories(uploadDir);
      Path uploadPath = uploadDir.resolve(filename);
      Files.copy(originPath, uploadPath, StandardCopyOption.REPLACE_EXISTING);

      // 6) 브라우저 접근용 URL 반환
      return "/upload/" + yyyy + "/" + MM + "/" + dd + "/" + filename;

    } catch (IOException e) {
      throw new IllegalStateException("파일 저장에 실패했습니다.");
    }
  }

  public void deleteByPublicUrl(String publicUrl) {
    if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
      return;
    }
    String relative = publicUrl.substring("/uploads/".length()); // yyyy/MM/dd/filename
    Path baseUpload = Paths.get(uploadRoot).toAbsolutePath();
    Path target = baseUpload.resolve(relative);
    try {
      Files.deleteIfExists(target);
    } catch (IOException ignored) {
    }
  }

  public String getAbsoluteUrl(String relativePath) {
    if (relativePath == null || relativePath.isBlank()) {
      return null;
    }
    if (relativePath.startsWith("/uploads")) {
      return relativePath;
    }
    return (relativePath.startsWith("/") ? "" : "/") + relativePath;
  }
}
