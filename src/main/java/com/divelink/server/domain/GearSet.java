package com.divelink.server.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GearSet {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

//  @Column(nullable = false)
//  private String name;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private String createdBy;

  @OneToMany(mappedBy = "gearSet", cascade = CascadeType.ALL)
  private List<GearSetMapping> mappings = new ArrayList<>();
}
