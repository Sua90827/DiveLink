package com.divelink.server.controller;

import com.divelink.server.dto.DiveLogRequest;
import com.divelink.server.service.DiveLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class DiveLogController {

  private final DiveLogService diveLogService;

  @PostMapping("write")
  public ResponseEntity<String> writeLog(@RequestBody DiveLogRequest request){
    try{
      diveLogService.saveLog(request);
      return ResponseEntity.status(HttpStatus.CREATED).body("저장 성공");
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저장 실패" + e.getMessage());
    }
  }
}
