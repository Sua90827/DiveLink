package com.divelink.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventNoticeRequest {
  private String title;
  private String content;
}
