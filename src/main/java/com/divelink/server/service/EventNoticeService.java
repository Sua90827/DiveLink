package com.divelink.server.service;

import static com.divelink.server.domain.EventApplication.EventApplicationStatus.ATTENDING;
import static com.divelink.server.domain.EventApplication.EventApplicationStatus.CANCELLED;

import com.divelink.server.domain.EventApplication;
import com.divelink.server.domain.EventImage;
import com.divelink.server.domain.EventNotice;
import com.divelink.server.domain.User;
import com.divelink.server.dto.EventApplicationResponse;
import com.divelink.server.dto.EventNoticeRequest;
import com.divelink.server.dto.EventNoticeResponse;
import com.divelink.server.repository.EventApplicationRepository;
import com.divelink.server.repository.EventImageRepository;
import com.divelink.server.repository.EventNoticeRepository;
import com.divelink.server.repository.UserRepository;
import com.divelink.server.storage.FileStorage;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@AllArgsConstructor
public class EventNoticeService {

  private final EventNoticeRepository eventNoticeRepository;
  private final EventApplicationRepository eventApplicationRepository;
  private final UserRepository userRepository;
  private final EventImageRepository eventImageRepository;
  private final FileStorage storage;
  private final ImageService imageService;

  @Transactional
  public Long create(String userId, EventNoticeRequest request) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원"));

    EventNotice e = EventNotice.builder()
        .title(request.getTitle())
        .content(request.getContent())
        .createdBy(user)
        .createdAt(LocalDateTime.now())
        .build();
    return eventNoticeRepository.save(e).getId();
  }

  @Transactional
  public Long createWithImages(String userId, String title, String content, List<MultipartFile> files, Integer coverIndex) {

    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원"));

    EventNotice event = eventNoticeRepository.save(EventNotice.builder()
        .title(title)
        .content(content)
        .createdBy(user)
        .createdAt(LocalDateTime.now())
        .build());

    if (files != null && !files.isEmpty()) {
      imageService.addImages(event, files, 0);
      var finalList = eventImageRepository.findByEventNoticeOrderBySortOrderAscIdAsc(event);
      imageService.applyOrderAndCover(finalList, (coverIndex == null) ? 0 : coverIndex);
    }
    return event.getId();
  }

  @Transactional(readOnly = true)
  public Page<EventNoticeResponse> eventNoticeList(Pageable pageable) {
    return eventNoticeRepository.findAll(pageable).map(e -> {
      String url = null;
      return new EventNoticeResponse(e.getId(), e.getTitle(), e.getContent(), e.getCreatedAt());
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
  public void updateWithImages(Long eventId, String title, String content, boolean replace, List<MultipartFile> files, Integer coverIndex) {
    EventNotice e = eventNoticeRepository.findById(eventId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지"));

    if (title != null)   e.setTitle(title);
    if (content != null) e.setContent(content);

    // 현재 이미지들 로드 (정렬 유지)
    List<EventImage> current = eventImageRepository
        .findByEventNoticeOrderBySortOrderAscIdAsc(e);

    //replace=true -> 기존 이미지 전부 삭제(+스토리지에서 삭제)
    if (replace) {
      imageService.deleteAllForEvent(e);
      current = new ArrayList<>();
    }

    if (files != null && !files.isEmpty()) {
      var added = imageService.addImages(e, files, current.size());
      current.addAll(added);
    }

    imageService.applyOrderAndCover(current, coverIndex); // coverIndex null이면 첫 번째가 커버
  }

  @Transactional
  public void delete(Long id, String userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원"));

    EventNotice event = eventNoticeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이벤트 글"));

    if (!event.getCreatedBy().getId().equals(user.getId())) {
      throw new AccessDeniedException("본인이 작성한 글만 삭제할 수 있습니다.");
    }

    for (EventImage img : event.getImages()) {
      try {
        storage.delete(img.getStorageKey());
      } catch (Exception ex) {
        log.warn("스토리지 사진 삭제 실패 : eventId = {}, imageId = {}, key = {}", id, img.getId(), img.getStorageKey(), ex);
      }
    }
    eventNoticeRepository.delete(event);
  }


  //이벤트 신청(ATTENDING). 이미 신청 상태면 기존 ID 반환
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
