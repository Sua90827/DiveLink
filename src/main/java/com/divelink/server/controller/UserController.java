package com.divelink.server.controller;

import com.divelink.server.config.JwtTokenProvider;
import com.divelink.server.dto.UserLoginRequest;
import com.divelink.server.dto.UserRegisterRequest;
import com.divelink.server.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody @Valid UserRegisterRequest request){
    try{
      boolean successRegister = userService.register(request.getUserId(), request.getName(), request.getBirthday(), request.getPassword());
      if(successRegister){
        return ResponseEntity.ok("회원가입 완료");
      }else{
        return ResponseEntity.status(HttpStatus.CONFLICT).body("등록 실패(이미 존재하는 아이디)");
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 등록 중 예외 발생: " + e.getMessage());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody @Valid UserLoginRequest request, HttpSession session){
    try{
      boolean loginSuccess = userService.login(request.getUserId(), request.getPassword());

      if(loginSuccess){
        String token = jwtTokenProvider.generateToken(request.getUserId());
        session.setAttribute("USER_ID", request.getUserId());
        return ResponseEntity.ok("로그인 성공. JWT: " + token);
      }else{
        //아이디/비번 불일치 등
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패: 잘못된 데이터");
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 처리 중 오류 발생: " + e.getMessage());
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpSession session){
    session.invalidate();
    return ResponseEntity.ok("로그아웃 완료");
  }

  @GetMapping("/session")
  private ResponseEntity<String> checkSession(HttpSession session){
    String userId = (String) session.getAttribute("USER_ID");
    if(userId != null){
      return ResponseEntity.ok("세션 있음 userId: " + userId);
    }else{
      return ResponseEntity.ok("세션 없음");
    }
  }
}
