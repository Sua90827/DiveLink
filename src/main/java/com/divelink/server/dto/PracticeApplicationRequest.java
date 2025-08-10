package com.divelink.server.dto;

import com.divelink.server.domain.PracticeApplication.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeApplicationRequest {
  private Long practiceNoticeId;       // 연습 공지 ID
  private Set<Long> gearIds;          // 신청한 장비 ID 목록
}