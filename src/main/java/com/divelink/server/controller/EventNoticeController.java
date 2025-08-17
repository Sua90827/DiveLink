package com.divelink.server.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import com.divelink.server.context.UserContext;
import com.divelink.server.dto.EventApplicationResponse;
import com.divelink.server.dto.EventNoticeRequest;
import com.divelink.server.dto.EventNoticeResponse;
import com.divelink.server.service.EventNoticeService;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.flogger.Flogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventNoticeController {
  private final EventNoticeService eventNoticeService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> create(@RequestBody EventNoticeRequest request) {
    Long id = eventNoticeService.create(UserContext.getUserId(), request);
    return ResponseEntity.ok("이벤트 글 작성 완료 id : " + id);
  }

  @Getter
  @Setter
  public static class EventCreateForm {
    private String title;
    private String content;
    private Integer coverIndex = 0; //최종 리스트 기준 0-based (선택)
  }
  // EventNoticeController 내
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> createWithImages(
      @ModelAttribute EventCreateForm form,
      @RequestParam(value = "files", required = false) List<MultipartFile> files
  ) {
    List<MultipartFile> safeFiles = (files == null) ? List.of() : files;

    int count = safeFiles.size();
    long totalSize = safeFiles.stream().mapToLong(MultipartFile::getSize).sum();

    if (count == 0) {
      log.info("[CTRL] files.size=0");
    } else {
      MultipartFile first = safeFiles.get(0);
      log.info("[CTRL] files.size={}, sumSize={}, firstSize={}, firstName={}, firstCT={}",
          count, totalSize, first.getSize(), first.getOriginalFilename(), first.getContentType());
    }

    Long id = eventNoticeService.createWithImages(
        UserContext.getUserId(), form.getTitle(), form.getContent(), files, form.getCoverIndex()
    );
    return ResponseEntity.ok("이벤트 글(이미지 포함) 작성 완료 id : " + id);
  }


  @GetMapping
  public ResponseEntity<Page<EventNoticeResponse>> eventNoticeList(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable) {
    Page<EventNoticeResponse> eventNotices = eventNoticeService.eventNoticeList(pageable);
    return ResponseEntity.ok(eventNotices);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<String> update(@PathVariable Long id, @RequestBody EventNoticeRequest request) {
    eventNoticeService.update(id, request);
    return ResponseEntity.ok("수정 성공 id : " + id);
  }

  // 이미지 포함 수정용 폼
  @Getter @Setter
  public static class EventUpdateForm {
    private String title;
    private String content;
    private Boolean replace = false; //true면 기존 이미지 전부 삭제 후 새로 업로드
    private Integer coverIndex; //최종 리스트 기준 0-based (선택)
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> updateWithImages(
      @PathVariable Long id,
      @ModelAttribute EventUpdateForm form,
      @RequestParam(value = "files", required = false) List<MultipartFile> files
  ) {
    eventNoticeService.updateWithImages(
        id,
        form.getTitle(),
        form.getContent(),
        Boolean.TRUE.equals(form.getReplace()),
        files,
        form.getCoverIndex()
    );
    return ResponseEntity.ok("수정(이미지 포함) 성공 id : " + id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<String> delete(@PathVariable Long id) {
    eventNoticeService.delete(id, UserContext.getUserId());
    return ResponseEntity.ok("삭제 완료");
  }

  //현재 로그인 사용자의 이벤트 신청
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{id}/apply")
  public ResponseEntity<String> apply(@PathVariable Long id) {
    Long appId = eventNoticeService.apply(id, UserContext.getUserId());
    return ResponseEntity.ok("신청 완료 applicationId : " + appId);
  }

  //관리자용 신청자 목록
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}/applications")
  public ResponseEntity<Page<EventApplicationResponse>> applications(@PathVariable Long id, @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable) {
    return ResponseEntity.ok(eventNoticeService.getApplications(id, pageable));
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{id}/apply")
  public ResponseEntity<String> cancel(@PathVariable Long id) {
    eventNoticeService.cancel(id, UserContext.getUserId());
    return ResponseEntity.ok("신청 취소 완료");
  }
}
