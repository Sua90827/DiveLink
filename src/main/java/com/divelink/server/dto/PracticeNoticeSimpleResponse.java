package com.divelink.server.dto;

import com.divelink.server.domain.PracticeNotice;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PracticeNoticeSimpleResponse {
  private Long id;
  private String title;
  private String location;

  public static PracticeNoticeSimpleResponse from(PracticeNotice notice) {
    return new PracticeNoticeSimpleResponse(
        notice.getId(),
        notice.getTitle(),
        notice.getLocation()
    );
  }
}
