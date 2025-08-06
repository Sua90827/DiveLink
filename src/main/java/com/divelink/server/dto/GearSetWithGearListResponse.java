package com.divelink.server.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GearSetWithGearListResponse {
  private Long setId;
  private List<GearResponse> gears;
}
