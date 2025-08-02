package com.divelink.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

  @NotBlank(message = "아이디를 입력하세요.")
  private String userId;

  @NotBlank(message = "비밀번호를 입력하세요.")
  private String password;
}
