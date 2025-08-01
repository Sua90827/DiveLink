package com.divelink.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
    http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 페이지 사용 안 함(postman으로 테스트 할거라서)
        .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증도 꺼야함
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/users/register", "/users/login", "/users/logout", "/users/session").permitAll()
            .anyRequest().authenticated()
        );

    return http.build();

  }

}
