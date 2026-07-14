package com.found404.delivery.domain.user.repository;

import com.found404.delivery.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // JwtAuthenticationFilter에서 매 요청마다 사용.
    // User 엔티티 전체를 로드하지 않고 tokenVersion 컬럼만 조회해서 비용을 최소화한다.
    @Query("select u.tokenVersion from User u where u.id = :userId")
    Optional<Long> findTokenVersionById(@Param("userId") Long userId);
}