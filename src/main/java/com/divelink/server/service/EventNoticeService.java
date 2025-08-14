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
import com.divelink.server.storage.FileStorage.UploadResult;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        .createdAt(LocalDateTime.now())//이미지도 추가하기
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
    String keyPrefix = "events/" + event.getId();

    for (int i = 0; i < files.size(); i++) {
      MultipartFile file = files.get(i);

      // 1) 파일 바이트로 한 번만 읽기 (여기서 IOException 처리)
      final byte[] bytes;
      try {
        bytes = file.getBytes();
      } catch (IOException e) {
        throw new UncheckedIOException("업로드 파일 읽기 실패: " + file.getOriginalFilename(), e);
      }
      System.out.println("[SVC] file=" + file.getOriginalFilename()
          + ", mpLen=" + file.getSize() + ", bytesLen=" + bytes.length);
      if (bytes.length == 0) {
        System.out.println("[SVC] EMPTY BYTES -> skip");
        continue;
      }

      // 2) 저장소에 업로드 (ByteArrayInputStream으로 전달)
      FileStorage.UploadResult result = storage.upload(
          new ByteArrayInputStream(bytes),
          bytes.length,
          file.getContentType(),
          keyPrefix
      );

      // 3) 이미지 크기 추출 (IOException 무시 가능)
      Integer width = null, height = null;
      try (var bis = new ByteArrayInputStream(bytes)) {
        var img = ImageIO.read(bis); // IOException 가능
        if (img != null) { width = img.getWidth(); height = img.getHeight(); }
      } catch (IOException e) {
        log.warn("ImageIO.read 실패: {} (무시하고 진행)", file.getOriginalFilename(), e);
      }

      // 4) DB 저장
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
