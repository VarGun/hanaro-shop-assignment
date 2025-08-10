package com.example.hanaro.service;

import com.example.hanaro.dto.ProductCreateRequest;
import com.example.hanaro.dto.ProductResponse;
import com.example.hanaro.entity.Product;
import com.example.hanaro.repository.ProductRepository;
import com.example.hanaro.service.storage.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

  private final ProductRepository productRepository;
  private final FileStorageService fileStorageService;

  public ProductResponse create(ProductCreateRequest req) {
    if (productRepository.existsByName(req.getName())) {
      throw new IllegalStateException("이미 존재하는 상품명입니다.");
    }

    String imageUrl = null;
    if (req.getImage() != null && !req.getImage().isEmpty()) {
      imageUrl = fileStorageService.save(req.getImage());
    }

    Product saved = productRepository.save(Product.builder()
        .name(req.getName())
        .description(req.getDescription())
        .price(req.getPrice())
        .stockQuantity(req.getStockQuantity())
        .imageUrl(imageUrl)
        .build());

    return ProductResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public ProductResponse get(Long id) {
    return productRepository.findById(id)
        .map(ProductResponse::from)
        .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
  }

  public ProductResponse update(Long id, ProductCreateRequest req) {
    Product p = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

    if (req.getName() != null && productRepository.existsByNameAndIdNot(req.getName(), id)) {
      throw new IllegalStateException("이미 존재하는 상품명입니다.");
    }

    if (req.getImage() != null && !req.getImage().isEmpty()) {
      if (p.getImageUrl() != null) {
        fileStorageService.deleteByPublicUrl(p.getImageUrl());
      }
      String newUrl = fileStorageService.save(req.getImage());
      p.changeImage(newUrl);
    }

    p.changeInfo(req.getName(), req.getDescription(), req.getPrice(), req.getStockQuantity());

    return ProductResponse.from(p);
  }

  public void delete(Long id) {
    Product p = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
    if (p.getImageUrl() != null) {
      fileStorageService.deleteByPublicUrl(p.getImageUrl());
    }
    productRepository.delete(p);
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> search(String keyword, Integer minPrice, Integer maxPrice) {
    List<Product> products = productRepository.search(
        (keyword == null || keyword.isBlank()) ? null : keyword,
        minPrice,
        maxPrice
    );
    return products.stream().map(ProductResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> list(String q) {
    boolean hasQuery = q != null && !q.isBlank();
    List<Product> products = hasQuery
        ? productRepository.findByNameContainingIgnoreCase(q.trim())
        : productRepository.findAll();
    return products.stream().map(ProductResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public Product getById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. id=" + id));
  }
}