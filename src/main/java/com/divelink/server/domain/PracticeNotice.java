package com.divelink.server.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeNotice {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private LocalTime time;

  @Column(nullable = false)
  private String location;

  private Integer maxParticipants;

  @ManyToOne
  @JoinColumn(name = "gear_set_id", nullable = false)
  private GearSet gearSet;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentType paymentType;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "practiceNotice", cascade = CascadeType.ALL)
  private List<PracticeApplication> applications = new ArrayList<>();

  public enum PaymentType{
    FREE, INDIVIDUAL, FLAT
  }
}
