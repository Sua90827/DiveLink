package com.divelink.server.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import com.divelink.server.context.UserContext;
import com.divelink.server.domain.EventNotice;
import com.divelink.server.dto.EventNoticeRequest;
import com.divelink.server.dto.EventNoticeResponse;
import com.divelink.server.service.EventNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventNoticeController {
  private final EventNoticeService eventNoticeService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<String> create(@RequestBody EventNoticeRequest request) {
    Long id = eventNoticeService.create(UserContext.getUserId(), request);
    return ResponseEntity.ok("이벤트 글 작성 완료 id : " + id);
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

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<String> delete(@PathVariable Long id) {
    eventNoticeService.delete(id, UserContext.getUserId());
    return ResponseEntity.ok("삭제 완료");
  }

  

}
