package com.example.hanaro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ProductCreateRequest {

  @NotBlank(message = "상품명은 필수입니다.")
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "상품명")
  @Size(max = 100, message = "상품명은 최대 100자까지 가능합니다.")
  private String name;

  @Size(max = 1000, message = "설명은 최대 1000자까지 가능합니다.")
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "상품 설명")
  private String description;

  @NotNull(message = "가격은 필수입니다.")
  @Positive(message = "가격은 1원 이상이어야 합니다.")
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "상품 가격")
  private Integer price;

  @NotNull(message = "재고는 필수입니다.")
  @PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "상품 재고")
  private Integer stockQuantity;

  @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "상품 이미지")
  private MultipartFile image;
}