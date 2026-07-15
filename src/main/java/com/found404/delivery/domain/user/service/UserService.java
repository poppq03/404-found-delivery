package com.found404.delivery.domain.user.service;

import com.found404.delivery.domain.user.dto.*;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import com.found404.delivery.domain.user.repository.UserRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import com.found404.delivery.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션
public class UserService {

    // 정렬 허용 필드: 그 외 필드로 정렬 요청이 오면 무시하고 기본 정렬(createdAt desc)을 적용
    private static final Set<String> ALLOWED_SORT = Set.of("createdAt");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        validateRole(request.getRole());
        validateDuplicate(request.getUsername(), request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.create(
                request.getUsername(),
                encodedPassword,
                request.getEmail(),
                request.getNickname(),
                request.getPhone(),
                request.getRole()
        );

        User savedUser = userRepository.save(user);

        return SignupResponseDto.from(savedUser);
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.createToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getTokenVersion()
        );

        return new LoginResponseDto(accessToken);
    }

    // 내 정보 조회
    public UserResponseDto getMyInfo(Long userId) {
        User user = getUserOrThrow(userId);
        return UserResponseDto.from(user);
    }

    // 내 정보(닉네임/전화번호/프로필이미지) 수정
    @Transactional
    public UserResponseDto updateMyInfo(Long userId, UserUpdateRequestDto request) {
        User user = getUserOrThrow(userId);

        user.updateProfile(request.getNickname(), request.getPhone(), request.getProfileImage());

        return UserResponseDto.from(user);
    }

    // 비밀번호 변경 (본인 확인용 현재 비밀번호 필수)
    @Transactional
    public void changePassword(Long userId, PasswordUpdateRequestDto request) {
        User user = getUserOrThrow(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedNewPassword);
    }

    // 회원 탈퇴 (Soft Delete). tokenVersion도 같이 증가해서 기존 토큰을 즉시 무효화.
    @Transactional
    public void withdraw(Long userId) {
        User user = getUserOrThrow(userId);
        user.withdraw(userId);
    }

    // 관리자용 유저 목록 검색 (MANAGER, MASTER만 접근 가능)
    public UserListResponseDto searchUsers(String requesterRole, String keyword, Role roleFilter, Pageable pageable) {
        validateAdminAccess(requesterRole);

        // 정렬: 허용 필드(createdAt)만 인정, 그 외는 버림 (Menu 도메인과 동일한 방침)
        List<Sort.Order> orders = pageable.getSort().stream()
                .filter(order -> ALLOWED_SORT.contains(order.getProperty()))
                .collect(Collectors.toList());

        if (orders.isEmpty()) {
            orders.add(Sort.Order.desc("createdAt"));
        }

        // size는 10/30/50만 허용, 그 외 값이 오면 10으로 고정
        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }

        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), size, Sort.by(orders));

        // keyword null/공백이면 전체 조회되도록 LIKE 패턴을 와일드카드로 처리
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? "%"
                : "%" + keyword + "%";

        Page<User> userPage = userRepository.search(keywordPattern, roleFilter, safePageable);
        return UserListResponseDto.from(userPage);
    }

    // 관리자용 특정 유저 단건 조회 (MANAGER, MASTER만 접근 가능)
    public UserResponseDto getUserByAdmin(String requesterRole, Long targetUserId) {
        validateAdminAccess(requesterRole);
        User user = getUserOrThrow(targetUserId);
        return UserResponseDto.from(user);
    }

    // 관리자 전용 API 공통 권한 체크: MANAGER, MASTER만 통과
    private void validateAdminAccess(String role) {
        if (!Role.MANAGER.name().equals(role) && !Role.MASTER.name().equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    // 공통: userId로 유저 조회, 없으면(또는 이미 탈퇴됐으면 @SQLRestriction에 의해 조회 안 됨) 404
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateRole(Role role) {
        if (role != Role.CUSTOMER && role != Role.OWNER) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
        }
    }

    private void validateDuplicate(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}