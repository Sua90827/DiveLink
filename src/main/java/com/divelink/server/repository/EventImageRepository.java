package com.divelink.server.repository;

import com.divelink.server.domain.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {

  EventImage findFirstByEventNoticeIdAndCoverTrueOrderBySortOrderAscIdAsc(Long eventNoticeId);
}
