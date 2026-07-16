package com.found404.delivery.global.config;

import com.found404.delivery.domain.user.repository.UserRepository;
import com.found404.delivery.global.security.jwt.JwtAuthenticationFilter;
import com.found404.delivery.global.security.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public SecurityConfig(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT는 세션을 안 쓰니까 CSRF 보호 불필요
                .csrf(csrf -> csrf.disable())

                // 세션을 아예 안 만들게 함 (JWT는 매 요청마다 토큰으로 인증하니까)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/regions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores/categories/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores/*/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores/*/menus").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/menus/*").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 우리가 만든 JWT 필터를, 원래 있던 로그인 필터 자리보다 앞에 끼워넣기
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, userRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}