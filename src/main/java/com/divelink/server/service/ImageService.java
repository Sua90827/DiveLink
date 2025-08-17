package com.divelink.server.service;

import com.divelink.server.domain.EventImage;
import com.divelink.server.domain.EventNotice;
import com.divelink.server.repository.EventImageRepository;
import com.divelink.server.storage.FileStorage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {
  private final FileStorage storage;
  private final EventImageRepository eventImageRepository;

  private static final String PREFIX = "uploads/events"; // 키 프리픽스 단일화

  public List<EventImage> addImages(EventNotice event, List<MultipartFile> files, int baseSort) {
    if (files == null || files.isEmpty()) return List.of();

    String keyPrefix = PREFIX + "/" + event.getId();
    List<EventImage> saved = new ArrayList<>();

    for (int i = 0; i < files.size(); i++) {
      MultipartFile f = files.get(i);
      byte[] bytes;
      try { bytes = f.getBytes(); } catch (IOException e) {
        throw new UncheckedIOException("업로드 파일 읽기 실패: " + f.getOriginalFilename(), e);
      }
      if (bytes.length == 0) continue;

      var result = storage.upload(new ByteArrayInputStream(bytes), bytes.length, f.getContentType(), keyPrefix);

      Integer w = null, h = null;
      try (var bis = new ByteArrayInputStream(bytes)) {
        var img = ImageIO.read(bis);
        if (img != null) { w = img.getWidth(); h = img.getHeight(); }
      } catch (IOException ignore) {}

      var entity = EventImage.builder()
          .eventNotice(event)
          .storageKey(result.key())
          .originalFilename(f.getOriginalFilename())
          .contentType(f.getContentType())
          .sizeBytes((long) bytes.length)
          .width(w).height(h)
          .sortOrder(baseSort + i)
          .cover(false)
          .build();

      saved.add(eventImageRepository.save(entity));
    }
    return saved;
  }

  public void deleteAllForEvent(EventNotice event) {
    var images = eventImageRepository.findByEventNoticeOrderBySortOrderAscIdAsc(event);
    for (EventImage img : images) {
      try { storage.delete(img.getStorageKey()); } catch (Exception ignore) {}
    }
    eventImageRepository.deleteAll(images);
  }

  /** 최종 리스트에 정렬/커버 적용 */
  public void applyOrderAndCover(List<EventImage> images, Integer coverIndex) {
    for (int i = 0; i < images.size(); i++) {
      images.get(i).setSortOrder(i);
      images.get(i).setCover(false);
    }
    if (images.isEmpty()) return;

    if (coverIndex != null && coverIndex >= 0 && coverIndex < images.size()) {
      images.get(coverIndex).setCover(true);
    } else {
      // 커버 없으면 첫 번째로
      images.get(0).setCover(true);
    }
  }
}
