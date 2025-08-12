package com.example.hanaro.service;

import com.example.hanaro.dto.ProductCreateRequest;
import com.example.hanaro.dto.ProductResponse;
import com.example.hanaro.entity.Product;
import com.example.hanaro.repository.CartItemRepository;
import com.example.hanaro.repository.OrderItemRepository;
import com.example.hanaro.repository.ProductRepository;
import com.example.hanaro.service.storage.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

  private static final Logger bizLog = LoggerFactory.getLogger("business.product");

  private final ProductRepository productRepository;
  private final FileStorageService fileStorageService;

  private final CartItemRepository cartItemRepository;
  private final OrderItemRepository orderItemRepository;

  public ProductResponse create(ProductCreateRequest req) {
    System.out.println(
        "PRODUCT 호출 PRODUCT 호출 PRODUCT 호출 PRODUCT 호출 PRODUCT 호출 PRODUCT 호출 PRODUCT 호출 PRODUCT 호출 ");
    bizLog.info("[PRODUCT][CREATE][REQ] name='{}', price={}, stock={}, hasImage={}", req.getName(),
        req.getPrice(), req.getStockQuantity(),
        (req.getImage() != null && !req.getImage().isEmpty()));
    if (productRepository.existsByName(req.getName())) {
      throw new IllegalStateException("이미 존재하는 상품명입니다.");
    }

    String imageUrl = null;
    if (req.getImage() != null && !req.getImage().isEmpty()) {
      imageUrl = fileStorageService.save(req.getImage());
    }
    if (imageUrl != null) {
      bizLog.info("[PRODUCT][IMAGE][SAVE] name='{}' -> {}", req.getName(), imageUrl);
    }

    Product saved = productRepository.save(Product.builder()
        .name(req.getName())
        .description(req.getDescription())
        .price(req.getPrice())
        .stockQuantity(req.getStockQuantity())
        .imageUrl(imageUrl)
        .build());

    bizLog.info("[PRODUCT][CREATE][RES] id={}, name='{}', price={}, stock={}, imageUrl={}",
        saved.getId(), saved.getName(), saved.getPrice(), saved.getStockQuantity(),
        saved.getImageUrl());

    ProductResponse response = ProductResponse.from(saved);
    if (response.getImageUrl() != null) {
      response.setImageUrl(toAbsoluteUrl(response.getImageUrl()));
    }
    return response;
  }

  @Transactional(readOnly = true)
  public ProductResponse get(Long id) {
    return productRepository.findById(id)
        .map(p -> {
          ProductResponse resp = ProductResponse.from(p);
          if (resp.getImageUrl() != null) {
            resp.setImageUrl(toAbsoluteUrl(resp.getImageUrl()));
          }
          return resp;
        })
        .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
  }

  public ProductResponse update(Long id, ProductCreateRequest req) {
    bizLog.info("[PRODUCT][UPDATE][REQ] id={}, name='{}', price={}, stock={}, changeImage={}", id,
        req.getName(), req.getPrice(), req.getStockQuantity(),
        (req.getImage() != null && !req.getImage().isEmpty()));
    Product p = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

    if (req.getName() != null && productRepository.existsByNameAndIdNot(req.getName(), id)) {
      throw new IllegalStateException("이미 존재하는 상품명입니다.");
    }

    if (req.getImage() != null && !req.getImage().isEmpty()) {
      bizLog.info("[PRODUCT][IMAGE][DELETE] id={}, oldUrl={}", id, p.getImageUrl());
      if (p.getImageUrl() != null) {
        fileStorageService.deleteByPublicUrl(p.getImageUrl());
      }
      String newUrl = fileStorageService.save(req.getImage());
      bizLog.info("[PRODUCT][IMAGE][SAVE] id={}, newUrl={}", id, newUrl);
      p.changeImage(newUrl);
    }

    p.changeInfo(req.getName(), req.getDescription(), req.getPrice(), req.getStockQuantity());

    ProductResponse response = ProductResponse.from(p);
    bizLog.info("[PRODUCT][UPDATE][RES] id={}, name='{}', price={}, stock={}, imageUrl={}",
        p.getId(), p.getName(), p.getPrice(), p.getStockQuantity(), p.getImageUrl());
    if (response.getImageUrl() != null) {
      response.setImageUrl(toAbsoluteUrl(response.getImageUrl()));
    }
    return response;
  }

  public void delete(Long id) {
    bizLog.info("[PRODUCT][DELETE][REQ] id={}", id);
    Product p = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

    boolean usedInCart = cartItemRepository.existsByProduct_Id(id);
    if (usedInCart) {
      throw new IllegalStateException("장바구니에 담긴 상품은 삭제할 수 없습니다.");
    }

    boolean usedInOrders = orderItemRepository.existsByProduct_Id(id);
    if (usedInOrders) {
      throw new IllegalStateException("주문에 사용된 상품은 삭제할 수 없습니다.");
    }

    if (p.getImageUrl() != null) {
      bizLog.info("[PRODUCT][IMAGE][DELETE] id={}, url={}", id, p.getImageUrl());
      fileStorageService.deleteByPublicUrl(p.getImageUrl());
    }
    productRepository.delete(p);
    bizLog.info("[PRODUCT][DELETE][RES] id={} done", id);
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> search(String keyword, Integer minPrice, Integer maxPrice) {
    List<Product> products = productRepository.search(
        (keyword == null || keyword.isBlank()) ? null : keyword,
        minPrice,
        maxPrice
    );
    return products.stream()
        .map(p -> {
          ProductResponse resp = ProductResponse.from(p);
          if (resp.getImageUrl() != null) {
            resp.setImageUrl(toAbsoluteUrl(resp.getImageUrl()));
          }
          return resp;
        })
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> list(String q) {
    boolean hasQuery = q != null && !q.isBlank();
    List<Product> products = hasQuery
        ? productRepository.findByNameContainingIgnoreCase(q.trim())
        : productRepository.findAll();
    return products.stream()
        .map(p -> {
          ProductResponse resp = ProductResponse.from(p);
          if (resp.getImageUrl() != null) {
            resp.setImageUrl(toAbsoluteUrl(resp.getImageUrl()));
          }
          return resp;
        })
        .toList();
  }

  @Transactional(readOnly = true)
  public Product getById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. id=" + id));
  }

  private String toAbsoluteUrl(String relativePath) {
    if (relativePath == null) {
      return null;
    }
    return fileStorageService.getAbsoluteUrl(relativePath);
  }

  @Transactional
  public void changeStock(Long productId, int delta) {
    Product p = productRepository.findById(productId)
        .orElseThrow(() -> new NoSuchElementException("상품이 없습니다."));
    bizLog.info("[PRODUCT][STOCK][REQ] id={}, delta={}, current={}", productId, delta,
        p.getStockQuantity());
    int next = p.getStockQuantity() + delta;
    if (next < 0) {
      throw new IllegalStateException("재고가 음수가 될 수 없습니다.");
    }
    p.setStockQuantity(next);
    bizLog.info("[PRODUCT][STOCK][RES] id={}, after={}", productId, next);
  }
}
