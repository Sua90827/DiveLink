package com.divelink.server.config;

import com.divelink.server.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = getJwtFromRequest(request);

    if (token != null) {
      log.info("JWT Token: {}", token);
    }

    try{
      if (token != null && jwtTokenProvider.validateToken(token)) {
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        String userRole = jwtTokenProvider.getUserRoleFromToken(token);

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(userRole));
        log.info("Authorities: {}", authorities);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //UserContext 설정
        UserContext.setUserId(userId);
        UserContext.setUserRole(userRole);
      }
      filterChain.doFilter(request, response);
    }finally {
      UserContext.clear();//요청 끝날 때 UserContext 초기화 - 메모리 누수 방지
    }
  }

  // 요청에서 JWT 토큰을 추출하는 메소드
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7); // "Bearer " 이후의 토큰만 반환
    }
    return null;
  }
}
