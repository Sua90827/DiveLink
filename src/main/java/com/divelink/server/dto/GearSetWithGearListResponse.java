package com.divelink.server.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class GearSetWithGearListResponse {
  private Long setId;
  private List<GearResponse> gears;

  public GearSetWithGearListResponse(Long setId, List<GearResponse> gears) {
    this.setId = setId;
    this.gears = gears;
  }
}
