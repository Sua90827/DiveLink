package com.divelink.server.service;

import com.divelink.server.domain.User;
import com.divelink.server.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User register(String userId, String name, String birthday, String pw){
    String encodedPw = encodePw(pw);
    User user = User.builder()
        .userId(userId)
        .name(name)
        .birthday(birthday)
        .password(encodedPw)
        .role(User.Role.USER) // 일반적인 경로로 회원가입 할 경우, 모두 일반 유저. 관리자는 개발자가 임의로 role 데이터 변경)
        .createdAt(LocalDateTime.now())
        .build();
    return userRepository.save(user);
  }

  private String encodePw(String pw) {
    return new BCryptPasswordEncoder().encode(pw);
  }

  public boolean login(String id, String password) {
    return userRepository.findByUserId(id)
        .map(user -> new BCryptPasswordEncoder().matches(password, user.getPassword()))
        .orElse(false);
  }
}
