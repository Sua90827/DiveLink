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
  public Long createWithImages(String userId, String title, String content,
      List<MultipartFile> files, Integer coverIndex) {

    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원"));

    EventNotice event = eventNoticeRepository.save(EventNotice.builder()
        .title(title)
        .content(content)
        .createdBy(user)
        .createdAt(LocalDateTime.now())
        .build());

    if (files == null || files.isEmpty()) {
      return event.getId();
    }

    int coverIdx = (coverIndex == null) ? 0 : Math.max(0, Math.min(coverIndex, files.size() - 1));
    String keyPrefix = "uploads/events/" + event.getId();

    for (int i = 0; i < files.size(); i++) {
      MultipartFile file = files.get(i);

      final byte[] bytes;
      try {
        bytes = file.getBytes();
      } catch (IOException e) {
        throw new UncheckedIOException("업로드 파일 읽기 실패: " + file.getOriginalFilename(), e);
      }
      if (bytes.length == 0) {
        continue;
      }

      //저장소에 업로드 (ByteArrayInputStream으로 전달)
      FileStorage.UploadResult result = storage.upload(
          new ByteArrayInputStream(bytes),
          bytes.length,
          file.getContentType(),
          keyPrefix
      );

      //이미지 크기 추출
      Integer width = null, height = null;
      try (var bis = new ByteArrayInputStream(bytes)) {
        var img = ImageIO.read(bis); // IOException 가능
        if (img != null) { width = img.getWidth(); height = img.getHeight(); }
      } catch (IOException e) {
        log.warn("ImageIO.read 실패: {} (무시하고 진행)", file.getOriginalFilename(), e);
      }

      EventImage image = EventImage.builder()
          .eventNotice(event)
          .storageKey(result.key())
          .originalFilename(file.getOriginalFilename())
          .contentType(file.getContentType())
          .sizeBytes((long) bytes.length)
          .width(width)
          .height(height)
          .sortOrder(i)
          .cover(i == coverIdx)
          .build();

      eventImageRepository.save(image);
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
  public void updateWithImages(Long eventId,
      String title,
      String content,
      boolean replace,
      List<MultipartFile> files,
      Integer coverIndex) {

    EventNotice e = eventNoticeRepository.findById(eventId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지"));

    if (title != null)   e.setTitle(title);
    if (content != null) e.setContent(content);

    // 현재 이미지들 로드 (정렬 유지)
    List<EventImage> current = eventImageRepository
        .findByEventNoticeOrderBySortOrderAscIdAsc(e);

    //replace=true -> 기존 이미지 전부 삭제(+스토리지에서 삭제)
    if (replace && !current.isEmpty()) {
      current.forEach(img -> storage.delete(img.getStorageKey()));
      eventImageRepository.deleteAll(current);
      current.clear();
    }

    //새 파일 업로드(있을 경우)
    List<EventImage> added = new ArrayList<>();
    if (files != null && !files.isEmpty()) {
      String keyPrefix = "uploads/events/" + e.getId();
      for (int i = 0; i < files.size(); i++) {
        MultipartFile f = files.get(i);
        byte[] bytes;
        try {
          bytes = f.getBytes();
        } catch (IOException ex) {
          throw new UncheckedIOException("업로드 파일 읽기 실패: " + f.getOriginalFilename(), ex);
        }
        if (bytes.length == 0) continue;

        //저장소 업로드
        var result = storage.upload(new ByteArrayInputStream(bytes), bytes.length, f.getContentType(), keyPrefix);

        //이미지 크기 추출(선택)
        Integer w = null, h = null;
        try (var bis = new ByteArrayInputStream(bytes)) {
          var img = ImageIO.read(bis);
          if (img != null) { w = img.getWidth(); h = img.getHeight(); }
        } catch (IOException ignore) {}

        var saved = eventImageRepository.save(EventImage.builder()
            .eventNotice(e)
            .storageKey(result.key())
            .originalFilename(f.getOriginalFilename())
            .contentType(f.getContentType())
            .sizeBytes((long) bytes.length)
            .width(w)
            .height(h)
            .sortOrder(0) // 일단 0으로, 아래에서 일괄 재정렬
            .cover(false)
            .build());
        added.add(saved);
      }
    }

    //최종 리스트 = (replace면 added만) / (아니면 기존 + added)
    List<EventImage> finalList = new ArrayList<>();
    finalList.addAll(current);
    finalList.addAll(added);

    //정렬/커버 재설정
    for (int i = 0; i < finalList.size(); i++) {
      finalList.get(i).setSortOrder(i);
      finalList.get(i).setCover(false);
    }
    if (coverIndex != null && coverIndex >= 0 && coverIndex < finalList.size()) {
      finalList.get(coverIndex).setCover(true);
    } else {
      //기존 커버 유지 로직 (replace=false이고 기존에 커버가 있었다면 유지)
      int prevCoverIdx = -1;
      for (int i = 0; i < current.size(); i++) {
        if (current.get(i).isCover()) { prevCoverIdx = i; break; }
      }
      if (prevCoverIdx >= 0 && prevCoverIdx < finalList.size()) {
        finalList.get(prevCoverIdx).setCover(true);
      } else if (!finalList.isEmpty()) {
        finalList.get(0).setCover(true); // 아무것도 없으면 첫 번째를 커버로
      }
    }
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
      try { storage.delete(img.getStorageKey()); } catch (Exception ignored) {}
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
