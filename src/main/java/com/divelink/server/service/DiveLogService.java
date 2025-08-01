package com.divelink.server.service;

import com.divelink.server.domain.DiveLog;
import com.divelink.server.dto.DiveLogRequest;
import com.divelink.server.repository.DiveLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiveLogService {

  private final DiveLogRepository diveLogRepository;

  public String saveLog(DiveLogRequest request) {


    try{
      DiveLog diveLog = DiveLog.builder()
          .userId(request.getUserId())
          .divingNo(request.getDivingNo())
          .date(request.getDate())
          .time(request.getTime())
          .location(request.getLocation())
          .suit(request.getSuit())
          .weight(request.getWeight())
          .fim(request.getFim())
          .cwt(request.getCwt())
          .dyn(request.getDyn())
          .longestDivingTime(request.getLongestDivingTime())
          .content(request.getContent())
          .comment(request.getComment())
          .createdAt(LocalDateTime.now())
          .build();

      diveLogRepository.save(diveLog);

      return "s";
    } catch(Exception e){
      return e.getMessage();
    }

  }
}
