package com.found404.delivery.domain.user.service;

import com.found404.delivery.domain.user.dto.LoginRequestDto;
import com.found404.delivery.domain.user.dto.LoginResponseDto;
import com.found404.delivery.domain.user.dto.SignupRequestDto;
import com.found404.delivery.domain.user.dto.SignupResponseDto;
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
                user.getRole().name()
        );

        return new LoginResponseDto(accessToken);
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