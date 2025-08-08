package com.divelink.server.dto;

import com.divelink.server.domain.PracticeNotice;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class PracticeNoticeResponse {

  private Long id;
  private String title;
  private LocalDate date;
  private LocalTime time;
  private String location;
  private Integer maxParticipants;
  private String paymentType;
  private String gearSetCreatedBy; // 필요하다면 gearSet에서 가져올 필드
  private String userId;

  public static PracticeNoticeResponse from(PracticeNotice entity) {
    return PracticeNoticeResponse.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .date(entity.getDate())
        .time(entity.getTime())
        .location(entity.getLocation())
        .maxParticipants(entity.getMaxParticipants())
        .paymentType(entity.getPaymentType().name())
        .gearSetCreatedBy(entity.getGearSet().getCreatedBy()) // 연관된 GearSet의 createdBy
        .userId(entity.getUserId())
        .build();
  }
}
