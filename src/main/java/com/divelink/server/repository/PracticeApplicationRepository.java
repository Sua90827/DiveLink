package com.divelink.server.repository;

import com.divelink.server.domain.PracticeApplication;
import com.divelink.server.domain.PracticeNotice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PracticeApplicationRepository extends JpaRepository<PracticeApplication, Long> {

  Optional<PracticeApplication> findByUserIdAndPracticeNotice(String userId, PracticeNotice practiceNotice);

  int deleteByIdAndUserId(Long practiceApplicationId, String userId);
}
