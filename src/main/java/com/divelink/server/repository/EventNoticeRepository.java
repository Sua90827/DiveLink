package com.divelink.server.repository;

import com.divelink.server.domain.EventNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventNoticeRepository extends JpaRepository<EventNotice, Long> {
}
