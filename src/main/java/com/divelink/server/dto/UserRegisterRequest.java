package com.divelink.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
  @NotBlank(message = "아이디는 필수입니다.")
  private String userId;

  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @NotBlank(message = "생년월일은 필수입니다.")
  @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "생년월일 형식은 yyyy-mm-dd입니다.")
  private String birthday;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(
      regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{7,}$",
      message = "비밀번호는 영문자, 숫자, 특수문자를 포함한 7자 이상이어야 합니다."
  )
  private String password;
}
