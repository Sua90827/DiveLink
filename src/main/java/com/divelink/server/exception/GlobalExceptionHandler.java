package com.divelink.server.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 요청: " + e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldError().getDefaultMessage();
    return ResponseEntity.badRequest().body("유효성 검사 실패: " + message);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDBError(DataIntegrityViolationException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("DB 제약 조건 위반: " + e.getMessage());
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<String> handleNotFound(EntityNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("엔티티 없음: " + e.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<String> handleJsonParse(HttpMessageNotReadableException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 본문(JSON) 파싱 실패: " + e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleServerError(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류: " + e.getMessage());
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ResponseEntity<String> handleDenied(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한 없음: " + e.getMessage());
  }
}
