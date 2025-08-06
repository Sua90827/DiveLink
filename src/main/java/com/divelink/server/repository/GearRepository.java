package com.divelink.server.repository;

import com.divelink.server.domain.Gear;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GearRepository  extends JpaRepository<Gear, Long> {

}