package com.divelink.server.repository;

import com.divelink.server.domain.DiveLog;
import com.divelink.server.dto.DiveLogCommentRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DiveLogRepository extends JpaRepository<DiveLog, Long> {

  List<DiveLog> findAllByUserId(String userId);

  @Modifying
  @Query("UPDATE DiveLog SET comment = :comment WHERE id = :id")
  void updateCommentById(Long id, String comment);
}
