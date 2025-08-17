package com.divelink.server.repository;

import com.divelink.server.domain.EventApplication;
import com.divelink.server.domain.EventNotice;
import com.divelink.server.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventApplicationRepository extends JpaRepository<EventApplication, Long> {
  Optional<EventApplication> findByUserAndEventNotice(User user, EventNotice eventNotice);

  Page<EventApplication> findByEventNotice(EventNotice eventNotice, Pageable pageable);
}
