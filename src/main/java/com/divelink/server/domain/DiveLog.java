package com.divelink.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)  // Auditing 기능 활성화
public class DiveLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private Integer divingNo;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private LocalTime time;

  @Column(nullable = false)
  private String location;

  private String suit;
  private Float weight;
  private Float fim;
  private Float cwt;
  private Float dyn;
  private String longestDivingTime;

  @Lob
  @Column(nullable = false)
  private String content;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Lob
  private String comment;

}
