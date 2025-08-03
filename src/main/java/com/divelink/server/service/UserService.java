package com.divelink.server.service;

import com.divelink.server.domain.User;
import com.divelink.server.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public boolean register(String userId, String name, String birthday, String pw){
    //이미 존재하는 아이디인지 확인
    Optional<User> existId = userRepository.findByUserId(userId);
    if(existId.isPresent()){
      return false;
    }

    String encodedPw = passwordEncoder.encode(pw);

    User user = User.builder()
        .userId(userId)
        .name(name)
        .birthday(birthday)
        .password(encodedPw)
        .role(User.Role.USER) // 일반적인 경로로 회원가입 할 경우, 모두 일반 유저. 관리자는 개발자가 임의로 role 데이터 변경)
        .createdAt(LocalDateTime.now())
        .build();
    userRepository.save(user);
    return true;
  }

  public boolean login(String id, String password) {
    return userRepository.findByUserId(id)
        .map(user -> passwordEncoder.matches(password, user.getPassword()))
        .orElse(false);
  }

  public String getUserRole(String userId) {
    return userRepository.findRoleByUserId(userId);
  }
}
