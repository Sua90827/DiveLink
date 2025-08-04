package com.divelink.server.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import com.divelink.server.context.UserContext;
import com.divelink.server.domain.DiveLog;
import com.divelink.server.dto.DiveLogCommentRequest;
import com.divelink.server.dto.DiveLogRequest;
import com.divelink.server.service.DiveLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class DiveLogController {

  private final DiveLogService diveLogService;

  private boolean isAdmin() {
    return "ADMIN".equals(UserContext.getUserRole());
  }

  // 다이빙 로그 작성
  @PostMapping
  public ResponseEntity<String> writeLog(@RequestBody DiveLogRequest request){
    try{
      String userId = UserContext.getUserId();
      if(!userId.equals(request.getUserId())){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      diveLogService.saveLog(request, userId);
      return ResponseEntity.status(HttpStatus.CREATED).body("저장 성공");
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저장 실패" + e.getMessage());
    }
  }

  // 사용자 본인 로그 목록 조회
  @GetMapping
  public ResponseEntity<Page<DiveLog>> diveLogList(@PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable){
    try{
      String userId = UserContext.getUserId();
      Page<DiveLog> diveLogs = diveLogService.getDiveLogList(userId, pageable);
      return ResponseEntity.ok(diveLogs);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  //사용자 본인 특정 로그 조회
  @GetMapping("/{id}")
  public ResponseEntity<DiveLog> userDiveLog(@PathVariable Long id){

    try {
      String userId = UserContext.getUserId();
      DiveLog diveLog = diveLogService.getDiveLog(userId, id);
      return ResponseEntity.ok(diveLog);
    }catch (IllegalArgumentException e){
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  //사용자 본인 로그 수정
  @PutMapping("/{id}")
  public ResponseEntity<String> modifyLog (@RequestBody DiveLogRequest request, @PathVariable Long id){
    try{
      String userId = UserContext.getUserId();
      diveLogService.modifyDiveLog(request, id, userId);
      return ResponseEntity.ok("수정 성공");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  //관리자가 특정 사용자의 다이빙 로그를 조회하는 API (comment 포함)
  @GetMapping("/admin")
  public ResponseEntity<Page<DiveLog>> adminDiveLogList(@RequestParam String userId,
      @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable){
    try{
      if(!isAdmin()){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      return ResponseEntity.ok(diveLogService.getDiveLogList(userId, pageable));

    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  //관리자가 특정 id의 다이빙 로그를 조회하는 API (comment 포함)
  @GetMapping("/admin/{id}")
  public ResponseEntity<DiveLog> adminDiveLog(@PathVariable Long id){
    try {
      if(!isAdmin()){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      DiveLog diveLog = diveLogService.getDiveLogByAdmin(id);
      return ResponseEntity.ok(diveLog);
    }catch (IllegalArgumentException e){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  //관리자 코멘트 작성
  @PostMapping("/{id}/comments")
  public ResponseEntity<String> writeComment(@PathVariable Long id, @RequestBody DiveLogCommentRequest comment){
    try{
      if(!isAdmin()){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한 문제");
      }
      diveLogService.saveComment(id, comment.getComment());
      return ResponseEntity.ok("comment 저장 완료");
    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  //관리자 코멘트 수정
  @PutMapping("/comments/{id}")
  public ResponseEntity<String> modifyComment(@PathVariable Long id, @RequestBody DiveLogCommentRequest comment){
    try{
      if(!isAdmin()){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한 문제");
      }
      diveLogService.saveComment(id, comment.getComment());
      return ResponseEntity.ok("comment 업데이트 완료");
    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  //관리자 코멘트 삭제
  @DeleteMapping("/comments/{id}")
  public ResponseEntity<String> deleteComment(@PathVariable Long id){
    try{
      if(!isAdmin()){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한 문제");
      }
      diveLogService.deleteComment(id);
      return ResponseEntity.ok("comment 삭제 완료");
    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
