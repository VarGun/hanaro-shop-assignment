package com.example.hanaro.repository;

import com.example.hanaro.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  @Query("""
      select u
      from User u
      where (:q is null
         or lower(u.email) like lower(concat('%', :q, '%'))
         or lower(u.name)  like lower(concat('%', :q, '%'))
         or u.phone like concat('%', :q, '%'))
      order by u.createdAt desc
      """)
  List<User> searchForAdmin(@Param("q") String keyword);
}