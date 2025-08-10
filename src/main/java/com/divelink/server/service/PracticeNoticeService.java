package com.divelink.server.service;

import com.divelink.server.domain.Gear;
import com.divelink.server.domain.GearSet;
import com.divelink.server.domain.PracticeApplication;
import com.divelink.server.domain.PracticeApplication.Status;
import com.divelink.server.domain.PracticeNotice;
import com.divelink.server.dto.PracticeApplicationRequest;
import com.divelink.server.dto.PracticeApplicationResponse;
import com.divelink.server.dto.PracticeNoticeRequest;
import com.divelink.server.dto.PracticeNoticeResponse;
import com.divelink.server.repository.GearRepository;
import com.divelink.server.repository.GearSetRepository;
import com.divelink.server.repository.PracticeApplicationRepository;
import com.divelink.server.repository.PracticeNoticeRepository;
import com.divelink.server.repository.projection.GearCapacityRow;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class PracticeNoticeService {

  private final PracticeNoticeRepository practiceNoticeRepository;
  private final PracticeApplicationRepository practiceApplicationRepository;
  private final GearSetRepository gearSetRepository;
  private final GearRepository gearRepository;

  public void writePracticeNotice(PracticeNoticeRequest request, String userId) {
    GearSet gearSet = gearSetRepository.findById(request.getGearSetId())
        .orElseThrow(()->new IllegalArgumentException("GearSet not found"));

    PracticeNotice notice = PracticeNotice.builder()
        .title(request.getTitle())
        .date(request.getDate())
        .time(request.getTime())
        .location(request.getLocation())
        .maxParticipants(request.getMaxParticipants())
        .gearSet(gearSet)
        .paymentType(request.getPaymentType())
        .userId(userId)
        .createdAt(LocalDateTime.now())
        .build();

    practiceNoticeRepository.save(notice);
  }

  public Page<PracticeNoticeResponse> getPracticeNoticeListById(String userId, Pageable pageable) {
    return practiceNoticeRepository.findAllByUserId(userId, pageable)
        .map(PracticeNoticeResponse::from);
  }

  public Page<PracticeNoticeResponse> getPracticeNoticeList(Pageable pageable) {
    return practiceNoticeRepository.findAll(pageable)
        .map(PracticeNoticeResponse::from);
  }

  @Transactional
  public void modifyPracticeNotice(Long id, PracticeNoticeRequest request) {
    PracticeNotice notice = practiceNoticeRepository.findById(id)
        .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 연습공지 id"));

    GearSet gearSet = gearSetRepository.findById(request.getGearSetId())
        .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 gearSet id"));

    notice.setTitle(request.getTitle());
    notice.setDate(request.getDate());
    notice.setTime(request.getTime());
    notice.setLocation(request.getLocation());
    notice.setMaxParticipants(request.getMaxParticipants());
    notice.setGearSet(gearSet);
    notice.setPaymentType(request.getPaymentType());
  }

  public void deletePracticeNotice(Long id) {
    practiceNoticeRepository.deleteById(id);
  }
  @Transactional
  public void apply(PracticeApplicationRequest request, String userId) {
    PracticeNotice practiceNotice = practiceNoticeRepository.findByIdForUpdate(request.getPracticeNoticeId())
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공지글"));

    Set<Long> requestedGearIds = request.getGearIds();

    //장비를 대여하지 않는 신청일 경우
    if(requestedGearIds == null || requestedGearIds.isEmpty()){
      PracticeApplication application = PracticeApplication.builder()
          .userId(userId)
          .practiceNotice(practiceNotice)
          .status(Status.APPLIED)
          .totalFee(0)
          .appliedAt(LocalDateTime.now())
          .gearIds(Collections.emptyList())
          .build();
      practiceApplicationRepository.save(application);
      return;
    }

    List<Long> ids = new ArrayList<>(requestedGearIds);

    // 2) 집계 한 번으로 capacity/used/price 가져오기
    List<GearCapacityRow> rows = gearRepository.checkCapacityForNotice(practiceNotice.getId(), ids);

    if (rows.size() != ids.size()) {
      throw new IllegalArgumentException("존재하지 않는 장비 ID 포함");
    }

    // 3) 재고 검증 + 총액 계산
    int totalFee = 0;
    for (GearCapacityRow r : rows) {
      int remain = (int) (r.getCapacity() - r.getUsed());
      if (remain <= 0) {
        throw new IllegalArgumentException("장비 재고 부족: gearId=" + r.getGearId());
      }
      totalFee += r.getPrice(); // 각 장비 1개씩 가정
    }

    PracticeApplication application = PracticeApplication.builder()
        .userId(userId)
        .practiceNotice(practiceNotice)
        .status(Status.PAYMENT_PENDING)
        .totalFee(totalFee)
        .appliedAt(LocalDateTime.now())
        .gearIds(ids)
        .build();
    practiceApplicationRepository.save(application);
  }

  public PracticeApplicationResponse getApplicationInfo(Long practiceNoticeId, String userId) {
    PracticeNotice practiceNotice = practiceNoticeRepository.findById(practiceNoticeId)
        .orElseThrow(()-> new EntityNotFoundException("공지글 없음"));

    PracticeApplication applicationInfo = practiceApplicationRepository.findByUserIdAndPracticeNotice(userId, practiceNotice)
        .orElseThrow(()->new EntityNotFoundException("신청 내역을 찾을 수 없음"));

    List<Long> gearIds = applicationInfo.getGearIds();
    List<Gear> gears = (gearIds == null)? Collections.emptyList() : gearRepository.findAllById(gearIds);

    return PracticeApplicationResponse.from(applicationInfo, gears);
  }

  @Transactional
  public void cancelApplication(Long applicationId, String userId) {
//    int affected = practiceApplicationRepository.deleteByIdAndUserId(practiceApplicationId, userId);
//    if(affected == 0){
//      throw new EntityNotFoundException("신청 내역이 없거나 이미 삭제됨");
//    }//DELETE 로직

//    PracticeApplication app = practiceApplicationRepository.findById(applicationId)
//        .orElseThrow(() -> new EntityNotFoundException("신청 내역 없음"));
//    if (!userId.equals(app.getUserId())) {
//      throw new IllegalArgumentException("본인 신청만 취소할 수 있습니다.");
//    }//기존 로직

    // 1) 신청 → 공지 ID 추출
    Long noticeId = practiceApplicationRepository.findNoticeIdByApplicationId(applicationId)
        .orElseThrow(() -> new EntityNotFoundException("신청 내역 없음"));

    // 2) 공지 행 락 (쓰기만 락)
    practiceNoticeRepository.findByIdForUpdate(noticeId)
        .orElseThrow(() -> new EntityNotFoundException("공지 없음"));

    // 3) 본인 신청 검증 + 상태 변경
    PracticeApplication app = practiceApplicationRepository.findByIdAndUserId(applicationId, userId)
        .orElseThrow(() -> new AccessDeniedException("본인 신청만 취소할 수 있습니다."));

    if(app.getStatus() == Status.PAYMENT_CONFIRMED) throw new RuntimeException("확정 이후 취소 불가");
    app.setStatus(Status.CANCELLED);
  }

  private static boolean counted(Status s) {
    return s == Status.PAYMENT_PENDING
        || s == Status.CONFIRM_REQUESTED
        || s == Status.PAYMENT_CONFIRMED;
  }
  @Transactional
  public void changeStatus(Long applicationId, PracticeApplication.Status status) {
    PracticeApplication application = practiceApplicationRepository.findWithNoticeById(applicationId)
        .orElseThrow(()-> new EntityNotFoundException("유효하지 않는 신청"));
    if(application.getStatus() == Status.PAYMENT_CONFIRMED){
      throw new IllegalArgumentException("확정 후 변경 불가");
    }
    // 집계 영향 전이이면 락 (혹은 항상 락)
    if (counted(application.getStatus()) || counted(status)) {
      practiceNoticeRepository.findByIdForUpdate(application.getPracticeNotice().getId())
          .orElseThrow(() -> new EntityNotFoundException("공지 없음"));
    }
    application.setStatus(status);
  }
}
