package com.divelink.server.controller;

import com.divelink.server.dto.UserLoginRequest;
import com.divelink.server.dto.UserRegisterRequest;
import com.divelink.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody @Valid UserRegisterRequest request){
    userService.register(request.getUserId(), request.getName(), request.getBirthday(), request.getPassword());
    return ResponseEntity.ok("회원가입 완료");
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody @Valid UserLoginRequest request, HttpServletRequest httpRequest){
    String result;

    boolean passed = userService.login(request.getUserId(), request.getPassword());
    if (passed) {
      HttpSession session = httpRequest.getSession(true);
      session.setAttribute("USER_ID", request.getUserId());
      return ResponseEntity.ok("로그인 완료");
    } else {
      return ResponseEntity.status(401).body("로그인 실패");
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
