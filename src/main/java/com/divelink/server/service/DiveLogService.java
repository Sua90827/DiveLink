package com.divelink.server.service;

import com.divelink.server.domain.DiveLog;
import com.divelink.server.dto.DiveLogRequest;
import com.divelink.server.repository.DiveLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiveLogService {

  private final DiveLogRepository diveLogRepository;

  public void saveLog(DiveLogRequest request) throws Exception{
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
          .build();

      diveLogRepository.save(diveLog);
    } catch(Exception e){
      throw new Exception("로그 저장 중 오류 발생: " + e.getMessage(), e);
    }
  }
}
