package com.found404.delivery.global.security.jwt;

import com.found404.delivery.domain.user.repository.UserRepository;
import com.found404.delivery.global.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.getClaims(token);

            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            Long tokenVersionInJwt = claims.get("tokenVersion", Long.class);

            // 토큰에 박힌 tokenVersion과 지금 DB에 저장된 tokenVersion을 비교한다.
            // User 엔티티 전체가 아니라 tokenVersion 컬럼만 조회해서 매 요청 비용을 최소화.
            // - DB 값과 다르면(role 변경/탈퇴 등으로 증가) 이 토큰은 이미 예전 권한 스냅샷이므로 인증 처리하지 않는다.
            // - 유저 자체가 없거나 삭제된 경우도 동일하게 인증 처리하지 않는다.
            boolean isTokenStillValid = userRepository.findTokenVersionById(userId)
                    .map(currentVersion -> currentVersion.equals(tokenVersionInJwt))
                    .orElse(false);

            if (isTokenStillValid) {
                CustomUserDetails userDetails = new CustomUserDetails(userId, username, "", role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}