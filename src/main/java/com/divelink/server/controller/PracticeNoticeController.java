package com.divelink.server.controller;

import static com.divelink.server.domain.PracticeApplication.Status.CANCELLED;
import static com.divelink.server.domain.PracticeApplication.Status.PAYMENT_CONFIRMED;
import static com.divelink.server.domain.PracticeApplication.Status.REJECTED;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.divelink.server.context.UserContext;
import com.divelink.server.domain.Gear;
import com.divelink.server.domain.PracticeApplication;
import com.divelink.server.domain.PracticeNotice;
import com.divelink.server.domain.User;
import com.divelink.server.dto.GearSetWithGearListResponse;
import com.divelink.server.dto.PracticeApplicationRequest;
import com.divelink.server.dto.PracticeApplicationResponse;
import com.divelink.server.dto.PracticeNoticeRequest;
import com.divelink.server.dto.PracticeNoticeResponse;
import com.divelink.server.service.GearService;
import com.divelink.server.service.PracticeNoticeService;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/practice")
@RequiredArgsConstructor
public class PracticeNoticeController {

  private final PracticeNoticeService practiceNoticeService;
  private final GearService gearService;

  //관리자 연습글 작성
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/notice")
  public ResponseEntity<String> writePracticeNotice(@RequestBody PracticeNoticeRequest request){
    String userId = UserContext.getUserId();
    practiceNoticeService.writePracticeNotice(request, userId);
    return ResponseEntity.ok("연습 공지 업로드 완료");
  }
  //관리자 연습글 조회
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/notice/admin")
  public ResponseEntity<Page<PracticeNoticeResponse>> adminPracticeNotices(@PageableDefault(size = 2, sort = "createdAt", direction = DESC) Pageable pageable){
    String userId = UserContext.getUserId();
    Page<PracticeNoticeResponse> practiceNotices = practiceNoticeService.getPracticeNoticeListById(userId, pageable);
    return ResponseEntity.ok(practiceNotices);
  }

  //관리자 연습글 수정
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/notice/{id}")
  public ResponseEntity<String> modifyPracticeNotice(@PathVariable Long id, @RequestBody PracticeNoticeRequest request){
    practiceNoticeService.modifyPracticeNotice(id, request);
    return ResponseEntity.ok("수정 완료");
  }

  //관리자 연습글 삭제
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("notice/{id}")
  public ResponseEntity<String> deletePracticeNotice(@PathVariable Long id){
    practiceNoticeService.deletePracticeNotice(id);
    return ResponseEntity.ok("삭제 완료");
  }

  //사용자 연습글 조회
  @GetMapping("/notice/user")
  public ResponseEntity<Page<PracticeNoticeResponse>> userPracticeNotices(@PageableDefault(size = 2, sort = "createdAt", direction = DESC) Pageable pageable){
    Page<PracticeNoticeResponse> practiceNotices = practiceNoticeService.getPracticeNoticeList(pageable);
    return ResponseEntity.ok(practiceNotices);
  }

  //사용자 장비 리스트 조회
  @GetMapping("/notice/gears")
  public ResponseEntity<GearSetWithGearListResponse> getGear(@RequestParam Long gearSetId) {
    GearSetWithGearListResponse gears = gearService.getGears(gearSetId);
    return ResponseEntity.ok(gears);
  }

  //사용자 연습 신청
  @PostMapping("/application")
  public ResponseEntity<String> applyPractice(@RequestBody PracticeApplicationRequest request){
    practiceNoticeService.apply(request, UserContext.getUserId());
    return ResponseEntity.ok("신청 완료");
  }

  //연습 신청 조회
  @GetMapping("/application")
  public ResponseEntity<PracticeApplicationResponse> getApplicationInfo(@RequestParam Long practiceNoticeId){
    PracticeApplicationResponse result = practiceNoticeService.getApplicationInfo(practiceNoticeId, UserContext.getUserId());
    return ResponseEntity.ok(result);
  }

  //연습 신청 취소
  @DeleteMapping("/application")
  public ResponseEntity<String> cancelApplication(@RequestParam Long practiceApplicationId){
    practiceNoticeService.cancelApplication(practiceApplicationId, UserContext.getUserId());
    return ResponseEntity.ok("신청 취소 완료");
  }

  //사용자 입금 상태 변경 (1, 2, 3)
  @PutMapping("/application/status")
  public ResponseEntity<String> userChangeStatus(@RequestParam Long applicationId, @RequestParam PracticeApplication.Status status){
    if(EnumSet.of(PAYMENT_CONFIRMED, REJECTED).contains(status)){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } else if (status == CANCELLED) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("/practice/application path api를 이용하세요.");
    }
    practiceNoticeService.changeStatus(applicationId, status);
    return ResponseEntity.ok("상태 변경 완료");
  }

  //관리자 입금 상태 변경 (4, 6)
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/admin/application/status")
  public ResponseEntity<String> adminChangeStatus(@RequestParam Long applicationId, @RequestParam PracticeApplication.Status status){
    if (!EnumSet.of(PAYMENT_CONFIRMED, REJECTED).contains(status)) {
      throw new IllegalArgumentException("허용되지 않은 변경값");
    }
    practiceNoticeService.changeStatus(applicationId, status);
    return ResponseEntity.ok("상태 변경 완료");
  }
}