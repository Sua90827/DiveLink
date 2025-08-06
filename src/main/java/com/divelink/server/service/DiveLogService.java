package com.divelink.server.service;

import com.divelink.server.domain.DiveLog;
import com.divelink.server.dto.DiveLogRequest;
import com.divelink.server.repository.DiveLogRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiveLogService {

  private final DiveLogRepository diveLogRepository;

  public void saveLog(DiveLogRequest request, String userId) {
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
  }

  public Page<DiveLog> getDiveLogList(String userId, Pageable pageable) {
    return diveLogRepository.findAllByUserId(userId, pageable);
  }

  public DiveLog getDiveLog(String userId, Long id) {
    return diveLogRepository.findById(id)
        .filter(log -> log.getUserId().equals(userId))
        .orElseThrow(() -> new IllegalArgumentException("권한이 없거나 존재하지 않는 로그입니다."));
  }

  public void modifyDiveLog(DiveLogRequest request, Long id, String userId){
    DiveLog existing = diveLogRepository.findById(id)
        .filter(diveLog -> diveLog.getUserId().equals(userId))
        .orElseThrow(() -> new IllegalArgumentException("권한이 없거나 존재하지 않는 로그입니다."));
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
