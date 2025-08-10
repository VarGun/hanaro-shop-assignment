package com.example.hanaro.config;

import com.example.hanaro.entity.Product;
import com.example.hanaro.entity.Role;
import com.example.hanaro.entity.User;
import com.example.hanaro.repository.ProductRepository;
import com.example.hanaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("local") // 로컬에서만 동작
@RequiredArgsConstructor
public class AdminSeedConfig {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ProductRepository productRepository;

  @Value("${app.admin.email}")
  private String adminEmail;
  @Value("${app.admin.password}")
  private String adminPassword;

  @Bean
  public ApplicationRunner adminSeeder() {
    return args -> {
      if (!userRepository.existsByEmail(adminEmail)) {
        userRepository.save(User.builder()
            .email(adminEmail)
            .password(passwordEncoder.encode(adminPassword))
            .name("ADMIN USER")
            .phone("010-0000-0000")
            .role(Role.ADMIN)
            .build());
        System.out.println("Admin user created: " + adminEmail);
      } else {
        System.out.println("Admin already exists: " + adminEmail);
      }

      // ==== TEMP SEEDING (LOCAL ONLY) - EASY TO REMOVE LATER ====
      // 1) Regular USER seeding
      String userEmail = "user1@aaa.com";
      if (!userRepository.existsByEmail(userEmail)) {
        userRepository.save(User.builder()
            .email(userEmail)
            .password(passwordEncoder.encode("Passw0rd!"))
            .name("USER1")
            .phone("010-0000-0000")
            .role(Role.USER)
            .build());
        System.out.println("[SEED] User created: " + userEmail);
      } else {
        System.out.println("[SEED] User already exists: " + userEmail);
      }

      // 2) Product seeding (by unique name)
      String seedProductName = "Product233";
      boolean existsProduct = productRepository.existsByName(seedProductName);
      if (!existsProduct) {
        Product p = Product.builder()
            .name(seedProductName)
            .description("product desc")
            .price(12000)
            .stockQuantity(20)
            .imageUrl(null)
            .build();
        productRepository.save(p);
        System.out.println("[SEED] Product created: " + seedProductName);
      } else {
        System.out.println("[SEED] Product already exists: " + seedProductName);
      }

      String seedProductName2 = "Product22";
      boolean existsProduct2 = productRepository.existsByName(seedProductName2);
      if (!existsProduct2) {
        Product p2 = Product.builder()
            .name(seedProductName2)
            .description("product desc")
            .price(12000)
            .stockQuantity(20)
            .imageUrl(null)
            .build();
        productRepository.save(p2);
        System.out.println("[SEED] Product created: " + seedProductName2);
      } else {
        System.out.println("[SEED] Product already exists: " + seedProductName2);
      }
      // ==== END TEMP SEEDING ====
    };
  }
}