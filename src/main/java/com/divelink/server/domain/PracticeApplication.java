package com.divelink.server.domain;

import com.divelink.server.converter.GearIdListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
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
public class PracticeApplication {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;

  @ManyToOne
  @JoinColumn(name = "practice_notice_id", nullable = false)
  private PracticeNotice practiceNotice;

  @Enumerated(EnumType.STRING)
  private Status status;

  private Integer totalFee;
  private LocalDateTime appliedAt;

  @Convert(converter = GearIdListConverter.class)
  @Column(columnDefinition = "TEXT") // MySQL 기준으로 JSON 저장 용도
  private List<Long> gearIds;

  public enum Status{
    APPLIED(1),
    PAYMENT_PENDING(2),
    CONFIRM_REQUESTED(3),
    PAYMENT_CONFIRMED(4),
    CANCELLED(5),
    REJECTED(6);

    private final int code;
    Status(int code) { this.code = code; }
    public int getCode() { return code; }

    public static Status fromCode(int code) {
      for (Status s : values()) if (s.code == code) return s;
      throw new IllegalArgumentException("잘못된 상태 코드: " + code);
    }
  }
}
