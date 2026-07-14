package com.found404.delivery.domain.user.service;

import com.found404.delivery.domain.user.dto.*;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import com.found404.delivery.domain.user.repository.UserRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import com.found404.delivery.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션
public class UserService {

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

    // 회원 탈퇴 (Soft Delete). tokenVersion도 같이 증가해서 기존 토큰을 즉시 무효화한다.
    @Transactional
    public void withdraw(Long userId) {
        User user = getUserOrThrow(userId);
        user.withdraw(userId);
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