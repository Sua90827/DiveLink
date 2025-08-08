package com.divelink.server.repository;

import com.divelink.server.domain.PracticeNotice;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PracticeNoticeRepository extends JpaRepository<PracticeNotice, Long> {

  Page<PracticeNotice> findAllByUserId(String userId, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select pn from PracticeNotice pn where pn.id = :id")
  Optional<PracticeNotice> findByIdForUpdate(@Param("id") Long id);
}
