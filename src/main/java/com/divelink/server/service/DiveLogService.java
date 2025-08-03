package com.divelink.server.service;

import com.divelink.server.domain.DiveLog;
import com.divelink.server.dto.DiveLogCommentRequest;
import com.divelink.server.dto.DiveLogRequest;
import com.divelink.server.repository.DiveLogRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiveLogService {

  private final DiveLogRepository diveLogRepository;

  public void saveLog(DiveLogRequest request, String userId) throws Exception{
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
          .build();

      diveLogRepository.save(diveLog);
    } catch(Exception e){
      throw new Exception("로그 저장 중 오류 발생: " + e.getMessage(), e);
    }
  }

  public List<DiveLog> getDiveLogList(String userId) {
    return diveLogRepository.findAllByUserId(userId);
  }

  public DiveLog getDiveLog(String userId, Long id) {
    return diveLogRepository.findById(id)
        .filter(log -> log.getUserId().equals(userId))
        .orElseThrow(() -> new IllegalArgumentException("권한이 없거나 존재하지 않는 로그입니다."));
  }

  public void modifyDiveLog(DiveLogRequest request, Long id, String userId) throws Exception{
    DiveLog existing = diveLogRepository.findById(id)
        .filter(diveLog -> diveLog.getUserId().equals(userId))
        .orElseThrow(() -> new IllegalArgumentException("권한이 없거나 존재하지 않는 로그입니다."));
    try{
      existing.setDate(request.getDate());
      existing.setTime(request.getTime());
      existing.setLocation(request.getLocation());
      existing.setSuit(request.getSuit());
      existing.setWeight(request.getWeight());
      existing.setFim(request.getFim());
      existing.setCwt(request.getCwt());
      existing.setDyn(request.getDyn());
      existing.setLongestDivingTime(request.getLongestDivingTime());
      existing.setContent(request.getContent());
      diveLogRepository.save(existing);
    }catch(Exception e){
      throw new Exception("로그 수정 중 오류 발생: " + e.getMessage(), e);
    }
  }

  public DiveLog getDiveLogByAdmin(Long id) {
    return diveLogRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 id의 로그가 존재하지 않습니다."));
  }

  @Transactional
  public void saveComment(Long id, String comment) {
    diveLogRepository.updateCommentById(id, comment);
  }

  @Transactional
  public void deleteComment(Long id) {
    diveLogRepository.updateCommentById(id, null);
  }
}
