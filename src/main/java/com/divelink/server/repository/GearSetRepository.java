package com.divelink.server.repository;

import com.divelink.server.domain.GearSet;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GearSetRepository extends JpaRepository<GearSet, Long> {

  List<GearSet> findAllByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);
}
