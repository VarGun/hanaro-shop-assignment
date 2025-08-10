// service/storage/FileStorageService.java
package com.example.hanaro.service.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  private final Path root;

  public FileStorageService(@Value("${app.file.upload-dir}") String uploadDir) throws IOException {
    this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    Files.createDirectories(this.root);
  }

  public String save(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }

    // 간단한 유형 체크 (png/jpg만 허용 예시)
    String contentType = file.getContentType();
    if (contentType == null || !(contentType.startsWith("image/"))) {
      throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
    }

    String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
    String filename = UUID.randomUUID() + (ext != null ? "." + ext : "");
    Path target = root.resolve(filename);

    try (InputStream in = file.getInputStream()) {
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("파일 저장에 실패했습니다.");
    }
    // 정적 서빙 경로와 맞추기 위해 /uploads/{filename} 반환
    return "/uploads/" + filename;
  }

  public void deleteByPublicUrl(String publicUrl) {
    if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
      return;
    }
    String fileName = publicUrl.substring("/uploads/".length());
    Path target = root.resolve(fileName);
    try {
      Files.deleteIfExists(target);
    } catch (IOException ignored) {
    }
  }
}