package com.divelink.server.controller;

import com.divelink.server.domain.DiveLog;
import com.divelink.server.dto.DiveLogCommentRequest;
import com.divelink.server.dto.DiveLogRequest;
import com.divelink.server.service.DiveLogService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import javax.swing.Spring;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class DiveLogController {

  private final DiveLogService diveLogService;

  @PostMapping("write")
  public ResponseEntity<String> writeLog(@RequestBody DiveLogRequest request, HttpSession session){
    try{
      if(session.getAttribute("USER_ID") == null || !session.getAttribute("USER_ID").equals(request.getUserId())){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      String userId = (String) session.getAttribute("USER_ID");
      diveLogService.saveLog(request, userId);
      return ResponseEntity.status(HttpStatus.CREATED).body("저장 성공");
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저장 실패" + e.getMessage());
    }
  }

  @GetMapping("read-list")
  public ResponseEntity<List<DiveLog>> diveLogList(HttpSession session){

    try{
      String userId = (String)session.getAttribute("USER_ID");
      List<DiveLog> diveLogList = diveLogService.getDiveLogList(userId);
      return ResponseEntity.ok(diveLogList);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("user-read-detail")
  public ResponseEntity<DiveLog> userDiveLog(@RequestParam Long id, HttpSession session){

    try {
      String userId = (String) session.getAttribute("USER_ID");
      if (userId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      DiveLog diveLog = diveLogService.getDiveLog(userId, id);
      return ResponseEntity.ok(diveLog);
    }catch (IllegalArgumentException e){
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("modify-log")
  public ResponseEntity<String> modifyLog (@RequestBody DiveLogRequest request, @RequestParam Long id, HttpSession session){
    try{
      if(session.getAttribute("USER_ID") == null){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      String userId = (String) session.getAttribute("USER_ID");
      diveLogService.modifyDiveLog(request, id, userId);
      return ResponseEntity.ok("수정 성공");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  //관리자가 특정 사용자의 다이빙 로그를 조회하는 API (comment 포함)
  @GetMapping("admin-read-list")
  public ResponseEntity<List<DiveLog>> diveLogList(@RequestParam String userId, HttpSession session){
    try{
      if(session.getAttribute("USER_ID") == null || !session.getAttribute("USER_ROLE").equals("ADMIN")){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      return ResponseEntity.ok(diveLogService.getDiveLogList(userId));

    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  //관리자가 특정 id의 다이빙 로그를 조회하는 API (comment 포함)
  @GetMapping("admin-read-log")
  public ResponseEntity<DiveLog> adminDiveLog(@RequestParam Long id, HttpSession session){
    try {
      if (session.getAttribute("USER_ID") == null || !session.getAttribute("USER_ROLE").equals("ADMIN")) {
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

  @PostMapping("write-comment")
  public ResponseEntity<String> writeComment(@RequestParam Long id, @RequestBody DiveLogCommentRequest comment, HttpSession session){
    try{
      if(session.getAttribute("USER_ID") == null || !session.getAttribute("USER_ROLE").equals("ADMIN")){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한 문제");
      }
      diveLogService.saveComment(id, comment.getComment());
      return ResponseEntity.ok("comment 저장 완료");
    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("modify-comment")
  public ResponseEntity<String> modifyComment(@RequestParam Long id, @RequestBody DiveLogCommentRequest comment, HttpSession session){
    try{
      if(session.getAttribute("USER_ID") == null || !session.getAttribute("USER_ROLE").equals("ADMIN")){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한 문제");
      }
      diveLogService.saveComment(id, comment.getComment());
      return ResponseEntity.ok("comment 업데이트 완료");
    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("delete-comment")
  public ResponseEntity<String> deleteComment(@RequestParam Long id, HttpSession session){
    try{
      if(session.getAttribute("USER_ID") == null || !"ADMIN".equals(session.getAttribute("USER_ROLE"))){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("권한 문제");
      }
      diveLogService.deleteComment(id);
      return ResponseEntity.ok("comment 삭제 완료");
    }catch (Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
