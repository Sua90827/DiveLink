package com.divelink.server.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GearRequest {
  private String name;
  private Integer price;
  private Integer quantity;
  private String createdBy;
}
