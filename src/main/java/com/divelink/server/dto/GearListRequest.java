package com.divelink.server.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GearListRequest {
  private List<GearRequest> gearList;
}
