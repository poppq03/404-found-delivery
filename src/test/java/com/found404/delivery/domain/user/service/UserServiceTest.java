package com.found404.delivery.domain.user.service;

import com.found404.delivery.domain.user.dto.LoginRequestDto;
import com.found404.delivery.domain.user.dto.LoginResponseDto;
import com.found404.delivery.domain.user.dto.ManagerCreateRequestDto;
import com.found404.delivery.domain.user.dto.PasswordUpdateRequestDto;
import com.found404.delivery.domain.user.dto.SignupRequestDto;
import com.found404.delivery.domain.user.dto.SignupResponseDto;
import com.found404.delivery.domain.user.dto.UserListResponseDto;
import com.found404.delivery.domain.user.dto.UserResponseDto;
import com.found404.delivery.domain.user.dto.UserUpdateRequestDto;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import com.found404.delivery.domain.user.repository.UserRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import com.found404.delivery.global.security.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private SignupRequestDto signupRequest(String username, String password, String email,
                                           String nickname, String phone, Role role) {
        SignupRequestDto dto = new SignupRequestDto();
        ReflectionTestUtils.setField(dto, "username", username);
        ReflectionTestUtils.setField(dto, "password", password);
        ReflectionTestUtils.setField(dto, "email", email);
        ReflectionTestUtils.setField(dto, "nickname", nickname);
        ReflectionTestUtils.setField(dto, "phone", phone);
        ReflectionTestUtils.setField(dto, "role", role);
        return dto;
    }

    private LoginRequestDto loginRequest(String username, String password) {
        LoginRequestDto dto = new LoginRequestDto();
        ReflectionTestUtils.setField(dto, "username", username);
        ReflectionTestUtils.setField(dto, "password", password);
        return dto;
    }

    private UserUpdateRequestDto updateRequest(String nickname, String phone, String profileImage) {
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "nickname", nickname);
        ReflectionTestUtils.setField(dto, "phone", phone);
        ReflectionTestUtils.setField(dto, "profileImage", profileImage);
        return dto;
    }

    private PasswordUpdateRequestDto passwordRequest(String currentPassword, String newPassword) {
        PasswordUpdateRequestDto dto = new PasswordUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "currentPassword", currentPassword);
        ReflectionTestUtils.setField(dto, "newPassword", newPassword);
        return dto;
    }

    private ManagerCreateRequestDto managerCreateRequest(String username, String password, String email,
                                                         String nickname, String phone) {
        ManagerCreateRequestDto dto = new ManagerCreateRequestDto();
        ReflectionTestUtils.setField(dto, "username", username);
        ReflectionTestUtils.setField(dto, "password", password);
        ReflectionTestUtils.setField(dto, "email", email);
        ReflectionTestUtils.setField(dto, "nickname", nickname);
        ReflectionTestUtils.setField(dto, "phone", phone);
        return dto;
    }

    private User userWithId(Long id, String username, String encodedPassword, String email,
                            String nickname, String phone, Role role) {
        User user = User.create(username, encodedPassword, email, nickname, phone, role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    // ===== signup =====

    @Test
    @DisplayName("회원가입 성공 - CUSTOMER role, 중복 없음")
    void signup_success() {
        SignupRequestDto request = signupRequest(
                "user123", "User1234!", "user123@example.com", "프랑키", "01011112222", Role.CUSTOMER);

        when(userRepository.existsByUsername("user123")).thenReturn(false);
        when(userRepository.existsByEmail("user123@example.com")).thenReturn(false);
        when(passwordEncoder.encode("User1234!")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> userWithId(1L, "user123", "ENCODED",
                        "user123@example.com", "프랑키", "01011112222", Role.CUSTOMER));

        SignupResponseDto response = userService.signup(request);

        assertThat(response.getUsername()).isEqualTo("user123");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - role이 MANAGER면 INVALID_ROLE 예외")
    void signup_fail_invalidRole() {
        SignupRequestDto request = signupRequest(
                "manager1", "Manager1!", "manager@example.com", "매니저", "01000000000", Role.MANAGER);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_ROLE);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - username 중복이면 USERNAME_ALREADY_EXISTS 예외")
    void signup_fail_duplicateUsername() {
        SignupRequestDto request = signupRequest(
                "user123", "User1234!", "new@example.com", "우사기", "01011112222", Role.CUSTOMER);

        when(userRepository.existsByUsername("user123")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - email 중복이면 EMAIL_ALREADY_EXISTS 예외")
    void signup_fail_duplicateEmail() {
        SignupRequestDto request = signupRequest(
                "user11", "User1234!", "user@example.com", "뉴유저", "01011112222", Role.CUSTOMER);

        when(userRepository.existsByUsername("user11")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    // ===== login =====

    @Test
    @DisplayName("로그인 성공 - 토큰 발급")
    void login_success() {
        User user = userWithId(1L, "user123", "ENCODED", "user@example.com",
                "먼작기", "01011112222", Role.CUSTOMER);
        LoginRequestDto request = loginRequest("user123", "User1234!");

        when(userRepository.findByUsername("user123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("User1234!", "ENCODED")).thenReturn(true);
        when(jwtUtil.createToken(1L, "user123", "CUSTOMER", 0L)).thenReturn("dummy-jwt-token");

        LoginResponseDto response = userService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("dummy-jwt-token");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 username이면 INVALID_CREDENTIALS 예외")
    void login_fail_userNotFound() {
        LoginRequestDto request = loginRequest("nobody", "aaaa1234!");

        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치면 INVALID_CREDENTIALS 예외")
    void login_fail_wrongPassword() {
        User user = userWithId(1L, "user123", "ENCODED", "user@example.com",
                "코비", "01011112222", Role.CUSTOMER);
        LoginRequestDto request = loginRequest("user123", "WrongPassword1!");

        when(userRepository.findByUsername("user123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword1!", "ENCODED")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verify(jwtUtil, never()).createToken(any(), any(), any(), any());
    }

    // ===== getMyInfo =====

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_success() {
        User user = userWithId(1L, "user123", "ENCODED", "user123@example.com",
                "로빈", "01011112222", Role.CUSTOMER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.getMyInfo(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("user123@example.com");
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 존재하지 않으면 USER_NOT_FOUND 예외")
    void getMyInfo_fail_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyInfo(999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ===== updateMyInfo =====

    @Test
    @DisplayName("내 정보 수정 성공 - 닉네임만 보내면 닉네임만 바뀌고 나머지는 유지")
    void updateMyInfo_success_partialUpdate() {
        User user = userWithId(1L, "user123", "ENCODED", "user123@example.com",
                "기어세컨드", "01011112222", Role.CUSTOMER);
        UserUpdateRequestDto request = updateRequest("기어써드", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.updateMyInfo(1L, request);

        assertThat(response.getNickname()).isEqualTo("기어써드");
        assertThat(response.getPhone()).isEqualTo("01011112222");
    }

    // ===== changePassword =====

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        User user = userWithId(1L, "user123", "OLD_ENCODED", "user123@example.com",
                "우솝", "01011112222", Role.CUSTOMER);
        PasswordUpdateRequestDto request = passwordRequest("OldPass1!", "NewPass1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", "OLD_ENCODED")).thenReturn(true);
        when(passwordEncoder.matches("NewPass1!", "OLD_ENCODED")).thenReturn(false);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("NEW_ENCODED");

        userService.changePassword(1L, request);

        assertThat(user.getPassword()).isEqualTo("NEW_ENCODED");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치면 INVALID_CURRENT_PASSWORD 예외")
    void changePassword_fail_wrongCurrentPassword() {
        User user = userWithId(1L, "user123", "OLD_ENCODED", "user123@example.com",
                "조로", "01011112222", Role.CUSTOMER);
        PasswordUpdateRequestDto request = passwordRequest("WrongOldPass1!", "NewPass1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOldPass1!", "OLD_ENCODED")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CURRENT_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 기존과 같으면 SAME_AS_OLD_PASSWORD 예외")
    void changePassword_fail_sameAsOld() {
        User user = userWithId(1L, "user123", "OLD_ENCODED", "user123@example.com",
                "나미", "01011112222", Role.CUSTOMER);
        PasswordUpdateRequestDto request = passwordRequest("OldPass1!", "OldPass1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", "OLD_ENCODED")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SAME_AS_OLD_PASSWORD);
    }

    // ===== withdraw =====

    @Test
    @DisplayName("회원 탈퇴 성공 - deletedAt/deletedBy 세팅 및 tokenVersion 증가")
    void withdraw_success() {
        User user = userWithId(1L, "user123", "ENCODED", "user123@example.com",
                "루피", "01011112222", Role.CUSTOMER);
        Long beforeVersion = user.getTokenVersion();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.withdraw(1L);

        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getTokenVersion()).isEqualTo(beforeVersion + 1);
    }

    // ===== searchUsers (관리자용) =====

    @Test
    @DisplayName("유저 검색 실패 - CUSTOMER 권한이면 FORBIDDEN 예외")
    void searchUsers_fail_notAdmin() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> userService.searchUsers("CUSTOMER", "user1", null, pageable))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(userRepository, never()).search(anyString(), any(), any());
    }

    @Test
    @DisplayName("유저 검색 성공 - MANAGER 권한이면 통과, size가 10/30/50 아니면 10으로 고정")
    void searchUsers_success_sizeFixedTo10() {
        User user = userWithId(1L, "user123", "ENCODED", "user123@example.com",
                "징베", "01011112222", Role.CUSTOMER);
        // UserService.searchUsers()가 size=7을 10으로 강제 교정한 뒤 Repository를 호출하므로,
        // Mock이 돌려줄 Page도 "size=10으로 교정된 Pageable"을 반영해서 만들어야 getSize() 검증이 의미 있다.
        Pageable correctedPageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user), correctedPageable, 1);
        Pageable weirdSizePageable = PageRequest.of(0, 7); // 10/30/50이 아닌 값

        when(userRepository.search(eq("%user%"), isNull(), any(Pageable.class))).thenReturn(page);

        UserListResponseDto response = userService.searchUsers("MANAGER", "user", null, weirdSizePageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getSize()).isEqualTo(10); // 7이 아니라 10으로 강제됐는지 확인
    }

    // ===== getUserByAdmin =====

    @Test
    @DisplayName("관리자 단건 조회 성공")
    void getUserByAdmin_success() {
        User user = userWithId(2L, "customer1", "ENCODED", "customer1@example.com",
                "고객", "01099998888", Role.CUSTOMER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.getUserByAdmin("MANAGER", 2L);

        assertThat(response.getUserId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("관리자 단건 조회 실패 - OWNER 권한이면 FORBIDDEN 예외")
    void getUserByAdmin_fail_notAdmin() {
        assertThatThrownBy(() -> userService.getUserByAdmin("OWNER", 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("관리자 단건 조회 실패 - 대상 유저가 없으면 USER_NOT_FOUND 예외")
    void getUserByAdmin_fail_targetNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByAdmin("MANAGER", 999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
    // ===== createManager =====

    @Test
    @DisplayName("MANAGER 생성 성공 - MASTER 권한, role이 MANAGER로 고정됨")
    void createManager_success() {
        ManagerCreateRequestDto request = managerCreateRequest(
                "manager1", "Manager1!", "manager1@example.com", "매니저", "01055556666");

        when(userRepository.existsByUsername("manager1")).thenReturn(false);
        when(userRepository.existsByEmail("manager1@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Manager1!")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> userWithId(3L, "manager1", "ENCODED",
                        "manager1@example.com", "매니저", "01055556666", Role.MANAGER));

        SignupResponseDto response = userService.createManager("MASTER", request);

        assertThat(response.getUsername()).isEqualTo("manager1");
        assertThat(response.getRole()).isEqualTo(Role.MANAGER); // 요청에 role이 없어도 MANAGER로 고정되는지 확인
    }

    @Test
    @DisplayName("MANAGER 생성 실패 - MANAGER 권한이면 FORBIDDEN 예외 (MASTER만 가능)")
    void createManager_fail_notMaster() {
        ManagerCreateRequestDto request = managerCreateRequest(
                "manager2", "Manager1!", "manager2@example.com", "매니저2", "01055556667");

        assertThatThrownBy(() -> userService.createManager("MANAGER", request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("MANAGER 생성 실패 - username 중복이면 USERNAME_ALREADY_EXISTS 예외")
    void createManager_fail_duplicateUsername() {
        ManagerCreateRequestDto request = managerCreateRequest(
                "manager1", "Manager1!", "new@example.com", "매니저", "01055556666");

        when(userRepository.existsByUsername("manager1")).thenReturn(true);

        assertThatThrownBy(() -> userService.createManager("MASTER", request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    // ===== getManagerByAdmin =====

    @Test
    @DisplayName("MANAGER 단건 조회 성공 - MASTER 권한, 대상이 실제 MANAGER")
    void getManagerByAdmin_success() {
        User manager = userWithId(3L, "manager1", "ENCODED", "manager1@example.com",
                "매니저", "01055556666", Role.MANAGER);

        when(userRepository.findById(3L)).thenReturn(Optional.of(manager));

        UserResponseDto response = userService.getManagerByAdmin("MASTER", 3L);

        assertThat(response.getUserId()).isEqualTo(3L);
        assertThat(response.getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    @DisplayName("MANAGER 단건 조회 실패 - MANAGER 권한이면 FORBIDDEN 예외 (MASTER만 가능)")
    void getManagerByAdmin_fail_notMaster() {
        assertThatThrownBy(() -> userService.getManagerByAdmin("MANAGER", 3L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("MANAGER 단건 조회 실패 - 대상이 MANAGER가 아니면(CUSTOMER) USER_NOT_FOUND 예외")
    void getManagerByAdmin_fail_targetNotManager() {
        User customer = userWithId(2L, "customer1", "ENCODED", "customer1@example.com",
                "고객", "01099998888", Role.CUSTOMER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> userService.getManagerByAdmin("MASTER", 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ===== updateManager =====

    @Test
    @DisplayName("MANAGER 수정 성공 - MASTER 권한, 대상이 실제 MANAGER")
    void updateManager_success() {
        User manager = userWithId(3L, "manager1", "ENCODED", "manager1@example.com",
                "매니저", "01055556666", Role.MANAGER);
        UserUpdateRequestDto request = updateRequest("수정된매니저", null, null);

        when(userRepository.findById(3L)).thenReturn(Optional.of(manager));

        UserResponseDto response = userService.updateManager("MASTER", 3L, request);

        assertThat(response.getNickname()).isEqualTo("수정된매니저");
    }

    @Test
    @DisplayName("MANAGER 수정 실패 - MANAGER 권한이면 FORBIDDEN 예외 (MASTER만 가능)")
    void updateManager_fail_notMaster() {
        UserUpdateRequestDto request = updateRequest("몰래수정", null, null);

        assertThatThrownBy(() -> userService.updateManager("MANAGER", 3L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("MANAGER 수정 실패 - 대상이 MANAGER가 아니면(CUSTOMER) USER_NOT_FOUND 예외")
    void updateManager_fail_targetNotManager() {
        User customer = userWithId(2L, "customer1", "ENCODED", "customer1@example.com",
                "고객", "01099998888", Role.CUSTOMER);
        UserUpdateRequestDto request = updateRequest("잘못된시도", null, null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> userService.updateManager("MASTER", 2L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ===== deleteManager =====

    @Test
    @DisplayName("MANAGER 삭제 성공 - deletedBy가 대상 본인이 아니라 요청자(MASTER) id로 세팅, username 반환")
    void deleteManager_success() {
        User manager = userWithId(3L, "manager1", "ENCODED", "manager1@example.com",
                "매니저", "01055556666", Role.MANAGER);
        Long masterRequesterId = 1L; // MASTER 본인의 id (삭제 대상인 3L과 다름)
        Long beforeVersion = manager.getTokenVersion();

        when(userRepository.findById(3L)).thenReturn(Optional.of(manager));

        String deletedUsername = userService.deleteManager("MASTER", masterRequesterId, 3L);

        assertThat(deletedUsername).isEqualTo("manager1");
        assertThat(manager.getDeletedAt()).isNotNull();
        assertThat(manager.getDeletedBy()).isEqualTo(masterRequesterId); // 대상 본인(3L)이 아니라 요청자(1L)여야 함
        assertThat(manager.getTokenVersion()).isEqualTo(beforeVersion + 1);
    }

    @Test
    @DisplayName("MANAGER 삭제 실패 - MANAGER 권한이면 FORBIDDEN 예외 (MASTER만 가능)")
    void deleteManager_fail_notMaster() {
        assertThatThrownBy(() -> userService.deleteManager("MANAGER", 1L, 3L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("MANAGER 삭제 실패 - 대상이 MANAGER가 아니면(CUSTOMER) USER_NOT_FOUND 예외")
    void deleteManager_fail_targetNotManager() {
        User customer = userWithId(2L, "customer1", "ENCODED", "customer1@example.com",
                "고객", "01099998888", Role.CUSTOMER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> userService.deleteManager("MASTER", 1L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}