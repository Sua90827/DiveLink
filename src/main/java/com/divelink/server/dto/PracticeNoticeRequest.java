package com.divelink.server.dto;

import com.divelink.server.domain.PracticeNotice.PaymentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeNoticeRequest {
  private String title;
  private LocalDate date;
  private LocalTime time;
  private String location;
  private Integer maxParticipants;
  private Long gearSetId;
  private PaymentType paymentType;
}
