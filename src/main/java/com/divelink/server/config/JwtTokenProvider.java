package com.divelink.server.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

  private final String SECRET_KEY = "mySuperSecretKeyThatIsAtLeast512BitsLongAndMoreSecureThanBeforemySuperSecretKeyThatIsAtLeast512BitsLongAndMoreSecureThanBefore";

  // JWT 토큰 생성
  public String generateToken(String userId) {
    return Jwts.builder()
        .setSubject(userId) // 사용자 정보
        .setIssuedAt(new Date()) // 토큰 발급 시간
        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 만료 시간 1일
        .signWith(SignatureAlgorithm.HS512, SECRET_KEY) // 서명
        .compact();
  }

  // JWT 토큰 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // JWT 토큰에서 사용자 정보 추출
  public String getUserIdFromToken(String token) {
    return Jwts.parser()
        .setSigningKey(SECRET_KEY)
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
}
