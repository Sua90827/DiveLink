package com.divelink.server.repository;

import com.divelink.server.domain.DiveLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DiveLogRepository extends JpaRepository<DiveLog, Long> {

  Page<DiveLog> findAllByUserId(String userId, Pageable pageable);

  @Modifying
  @Query("UPDATE DiveLog SET comment = :comment WHERE id = :id")
  void updateCommentById(Long id, String comment);
}
