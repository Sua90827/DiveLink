package com.divelink.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)  // Auditing 기능 활성화
@Table(name = "event_image",
    uniqueConstraints = {@UniqueConstraint(name = "uq_event_image_key", columnNames = {"storage_key"})},
    indexes = {@Index(name = "idx_event_image_event_id", columnList = "event_id, sort_order")})
public class EventImage {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  private EventNotice eventNotice;

  @Column(nullable = false)
  private String storageKey;

  private String originalFilename;
  private String contentType;
  private Long sizeBytes;
  private Integer width;
  private Integer height;

  @Column(nullable = false)
  private Integer sortOrder = 0;

  @Column(nullable = false)
  private boolean cover;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;

  private LocalDateTime deletedAt;
}
