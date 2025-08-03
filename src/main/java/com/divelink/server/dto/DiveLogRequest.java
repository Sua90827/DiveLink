package com.divelink.server.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiveLogRequest {
  private String userId; // 사용자 ID

  private Integer divingNo; // 다이빙 번호
  private LocalDate date; // 다이빙 날짜
  private LocalTime time; // 다이빙 시간
  private String location; // 다이빙 위치
  private String suit; // 착용한 수트
  private Float weight; // 체중

  private Float fim; // FIM
  private Float cwt; // CWT
  private Float dyn; // DYN

  private String longestDivingTime; // 최장 다이빙 시간

  private String content; // 다이빙 내용

//  private String comment; // 다이빙 코멘트
}
