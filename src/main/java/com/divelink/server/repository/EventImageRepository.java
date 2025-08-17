package com.divelink.server.repository;

import com.divelink.server.domain.EventImage;
import com.divelink.server.domain.EventNotice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {
  List<EventImage> findByEventNoticeOrderBySortOrderAscIdAsc(EventNotice e);
}
