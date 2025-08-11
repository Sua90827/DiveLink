package com.divelink.server.service;

import com.divelink.server.domain.EventNotice;
import com.divelink.server.domain.User;
import com.divelink.server.dto.EventNoticeRequest;
import com.divelink.server.dto.EventNoticeResponse;
import com.divelink.server.repository.EventImageRepository;
import com.divelink.server.repository.EventNoticeRepository;
import com.divelink.server.repository.UserRepository;
import com.divelink.server.storage.FileStorage;
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
  private final UserRepository userRepository;
  private final EventImageRepository eventImageRepository;
//  private final FileStorage storage;

  @Transactional
  public Long create(String userId, EventNoticeRequest request) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원"));

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
      return new EventNoticeResponse(e.getId(), e.getTitle(), e.getContent(), url, e.getCreatedAt());
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
            .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원"));
    eventNoticeRepository.deleteByIdAndCreatedBy(id, user);
  }


}
