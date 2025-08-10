package com.divelink.server.repository;

import com.divelink.server.domain.PracticeApplication;
import com.divelink.server.domain.PracticeNotice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PracticeApplicationRepository extends JpaRepository<PracticeApplication, Long> {

  Optional<PracticeApplication> findByUserIdAndPracticeNotice(String userId, PracticeNotice practiceNotice);

  int deleteByIdAndUserId(Long practiceApplicationId, String userId);

  Optional<PracticeApplication> findByIdAndUserId(Long id, String userId);

  // 공지 ID만 빠르게 얻어서 락에 활용 (불필요한 연관 로딩 최소화)
  @Query("select a.practiceNotice.id from PracticeApplication a where a.id = :appId")
  Optional<Long> findNoticeIdByApplicationId(@Param("appId") Long applicationId);

  // 필요시: 수정/취소 시 검증을 위해 함께 로딩
  @Query("select a from PracticeApplication a join fetch a.practiceNotice where a.id = :id")
  Optional<PracticeApplication> findWithNoticeById(@Param("id") Long id);
}
