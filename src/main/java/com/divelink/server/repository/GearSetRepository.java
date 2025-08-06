package com.divelink.server.repository;

import com.divelink.server.domain.GearSet;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GearSetRepository extends JpaRepository<GearSet, Long> {
  @Query("SELECT gs FROM GearSet gs " +
      "JOIN FETCH gs.mappings gsm " +
      "JOIN FETCH gsm.gear g " +
      "WHERE gs.createdBy = :createdBy " +
      "ORDER BY gs.createdAt DESC")
  List<GearSet> findWithMappingsAndGearsByCreatedBy(String createdBy, Pageable pageable);
}
