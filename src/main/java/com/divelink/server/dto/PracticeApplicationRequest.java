package com.divelink.server.dto;

import com.divelink.server.domain.PracticeApplication.Status;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeApplicationRequest {
  private Long practiceNoticeId;       // 연습 공지 ID
  private List<Long> gearIds;          // 신청한 장비 ID 목록
  private Status status;               // 신청 상태 (선택적 - 기본값이라면 생략 가능)
  private Integer totalFee;            // 총 대여 요금 (프론트에서 계산 or 서버 계산 여부에 따라 결정)
}