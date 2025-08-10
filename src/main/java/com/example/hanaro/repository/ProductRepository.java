package com.example.hanaro.repository;

import com.example.hanaro.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  boolean existsByName(String name);

  @Query("""
      select p from Product p
      where (:keyword is null or lower(p.name) like lower(concat('%', :keyword, '%'))
             or lower(p.description) like lower(concat('%', :keyword, '%')))
        and (:minPrice is null or p.price >= :minPrice)
        and (:maxPrice is null or p.price <= :maxPrice)
      """)
  List<Product> search(
      @Param("keyword") String keyword,
      @Param("minPrice") Integer minPrice,
      @Param("maxPrice") Integer maxPrice
  );

  boolean existsByNameAndIdNot(String name, Long id);

  List<Product> findByNameContainingIgnoreCase(String name);
}
