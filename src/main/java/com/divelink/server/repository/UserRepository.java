package com.divelink.server.repository;

import com.divelink.server.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUserId(String id);

  @Query("SELECT role FROM User WHERE userId = :userId")
  String findRoleByUserId(String userId);
}
