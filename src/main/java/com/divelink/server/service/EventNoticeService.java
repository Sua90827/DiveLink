package com.divelink.server.service;

import static com.divelink.server.domain.EventApplication.EventApplicationStatus.ATTENDING;
import static com.divelink.server.domain.EventApplication.EventApplicationStatus.CANCELLED;

import com.divelink.server.domain.EventApplication;
import com.divelink.server.domain.EventNotice;
import com.divelink.server.domain.User;
import com.divelink.server.dto.EventApplicationResponse;
import com.divelink.server.dto.EventNoticeRequest;
import com.divelink.server.dto.EventNoticeResponse;
import com.divelink.server.repository.EventApplicationRepository;
import com.divelink.server.repository.EventImageRepository;
import com.divelink.server.repository.EventNoticeRepository;
import com.divelink.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class EventNoticeService {

  private final EventNoticeRepository eventNoticeRepository;
  private final EventApplicationRepository eventApplicationRepository;
  private final UserRepository userRepository;
  private final EventImageRepository eventImageRepository;
//  private final FileStorage storage;

  @Transactional
  public Long create(String userId, EventNoticeRequest request) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원"));

    EventNotice e = EventNotice.builder()
        .title(request.getTitle())
        .content(request.getContent())
        .createdBy(user)
        .createdAt(LocalDateTime.now())//이미지도 추가하기
        .build();
    return eventNoticeRepository.save(e).getId();
  }

  @Transactional(readOnly = true)
  public Page<EventNoticeResponse> eventNoticeList(Pageable pageable) {
    return eventNoticeRepository.findAll(pageable).map(e -> {
//      var cover = eventImageRepository.findFirstByEventNoticeIdAndCoverTrueOrderBySortOrderAscIdAsc(e.getId());
//     String url = cover == null ? null : storage.getUrl(cover.getStorageKey());
      String url = null;
      return new EventNoticeResponse(e.getId(), e.getTitle(), e.getContent(), url,
          e.getCreatedAt());
    });
  }

  @Transactional
  public void update(Long eventId, EventNoticeRequest request) {
    EventNotice e = eventNoticeRepository.findById(eventId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지"));
    e.setTitle(request.getTitle());
    e.setContent(request.getContent());
  }

  @Transactional
  public void delete(Long id, String userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원"));
    eventNoticeRepository.deleteByIdAndCreatedBy(id, user);
  }


  //이벤트 신청(ATTENDING). 이미 신청 상태면 idempotent하게 기존 ID 반환
  @Transactional
  public Long apply(Long eventId, String userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원"));
    EventNotice event = eventNoticeRepository.findById(eventId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이벤트"));

    EventApplication application = eventApplicationRepository
        .findByUserAndEventNotice(user, event)
        .map(existing -> {
          existing.setStatus(ATTENDING); // 취소 이력이 있으면 복구
          return existing;
        })
        .orElseGet(() -> EventApplication.builder()
            .user(user)
            .eventNotice(event)
            .status(ATTENDING)
            .build());

    return eventApplicationRepository.save(application).getId();
  }

  @Transactional(readOnly = true)
  public Page<EventApplicationResponse> getApplications(Long eventId, Pageable pageable) {
    EventNotice event = eventNoticeRepository.findById(eventId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이벤트"));

    return eventApplicationRepository.findByEventNotice(event, pageable)
        .map(a -> new EventApplicationResponse(
            a.getId(),
            a.getUser().getUserId(),
            a.getUser().getName(),
            a.getStatus(),
            a.getCreatedAt()
        ));
  }

  @Transactional
  public void cancel(Long eventId, String userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원"));
    EventNotice event = eventNoticeRepository.findById(eventId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이벤트"));

    eventApplicationRepository.findByUserAndEventNotice(user, event)
        .ifPresent(application -> application.setStatus(CANCELLED));
  }
}
