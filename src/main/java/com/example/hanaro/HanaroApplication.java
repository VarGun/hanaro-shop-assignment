package com.example.hanaro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HanaroApplication {

  public static void main(String[] args) {
    SpringApplication.run(HanaroApplication.class, args);
  }

}
