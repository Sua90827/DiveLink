package com.divelink.server.controller;

import com.divelink.server.dto.DiveLogRequest;
import com.divelink.server.service.DiveLogService;
import lombok.RequiredArgsConstructor;
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
    String result;
    String saved = diveLogService.saveLog(request);
    if(saved.equals("s")){
      result = "저장 성공";
    }else{
      result = "저장 실패. " + saved;
    }
    return ResponseEntity.ok(result);
  }
}
