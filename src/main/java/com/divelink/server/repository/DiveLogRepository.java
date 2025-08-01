package com.divelink.server.repository;

import com.divelink.server.domain.DiveLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiveLogRepository extends JpaRepository<DiveLog, Long> {

}
