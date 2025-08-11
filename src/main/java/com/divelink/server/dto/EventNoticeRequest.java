package com.divelink.server.dto;

import com.divelink.server.domain.EventImage;
import com.divelink.server.domain.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventNoticeRequest {
  private String title;
  private String content;
}
