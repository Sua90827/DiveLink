package com.divelink.server.dto;

import com.divelink.server.domain.Gear;
import com.divelink.server.domain.PracticeApplication;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PracticeApplicationResponse {
  private Long id;
  private String userId;
  private PracticeNoticeSimpleResponse practiceNotice;
  private PracticeApplication.Status status;
  private Integer totalFee;
  private LocalDateTime appliedAt;
  private List<GearResponse> gears;

  public static PracticeApplicationResponse from(PracticeApplication app, List<Gear> gearList) {
    List<Gear> safeList = (gearList == null)? List.of() : gearList;
    return new PracticeApplicationResponse(
        app.getId(),
        app.getUserId(),
        PracticeNoticeSimpleResponse.from(app.getPracticeNotice()),
        app.getStatus(),
        app.getTotalFee(),
        app.getAppliedAt(),
        safeList.stream().map(GearResponse::new).toList()
    );
  }
}
