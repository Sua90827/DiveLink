package com.divelink.server.repository;

import com.divelink.server.domain.GearSetMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GearSetMappingRepository extends JpaRepository<GearSetMapping, Long> {

  List<GearSetMapping> findAllByGearSetId(Long setId);

  List<GearSetMapping> findAllByGearSetIdIn(List<Long> setIds);
}
