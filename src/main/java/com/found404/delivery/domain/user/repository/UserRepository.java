package com.found404.delivery.domain.user.repository;

import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // JwtAuthenticationFilter에서 매 요청마다 사용.
    @Query("select u.tokenVersion from User u where u.id = :userId")
    Optional<Long> findTokenVersionById(@Param("userId") Long userId);

    // 관리자용 유저 목록 검색. keyword는 username/nickname/email/phone 중 하나라도 일치하면 매칭.
    // 검색 조건이 복잡해지면 QueryDSL 고려 (Menu 도메인과 동일한 방침)
    @Query("""
            SELECT u FROM User u
            WHERE (u.username LIKE :keyword OR u.nickname LIKE :keyword OR u.email LIKE :keyword OR u.phone LIKE :keyword)
            AND (:role IS NULL OR u.role = :role)
            """)
    Page<User> search(@Param("keyword") String keyword,
                      @Param("role") Role role,
                      Pageable pageable);
}