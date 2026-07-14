package com.found404.delivery.domain.user.entity;

import com.found404.delivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 빈 객체 생성하는 거 방어.
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 로그인 id
    @Column(nullable = false, length = 10, unique = true)
    private String username;

    // 비밀번호
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // JWT 발급 시점의 권한 스냅샷과 현재 권한이 같은지 비교하기 위한 값
    // role이 바뀌거나 계정이 삭제되면 증가시켜서, 이전에 발급된 토큰을 즉시 무효화
    @Column(name = "token_version", nullable = false)
    private Long tokenVersion;

    public static User create(String username, String encodedPassword, String email,
                              String nickname, String phone, Role role) {
        User user = new User();
        user.username = username;
        user.password = encodedPassword;
        user.email = email;
        user.nickname = nickname;
        user.phone = phone;
        user.role = role;
        user.tokenVersion = 0L;
        return user;
    }

    // Setter 대신 구현함
    // 도메인 모델 더 안전하고 읽기 쉽게 만들기위함
    public void changeRole(Role role) {
        this.role = role;
        this.tokenVersion++;
    }

    // 탈퇴(Soft Delete) 처리. BaseEntity.markDeleted()와 별개로
    // tokenVersion도 함께 증가시켜, 삭제 이후 기존 토큰이 더 이상 통과되지 않도록 한다.
    public void withdraw(Long deletedBy) {
        markDeleted(deletedBy);
        this.tokenVersion++;
    }

    public void updateProfile(String nickname, String phone, String profileImage) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    // 비밀번호는 이미 암호화된 값만 받는다
    // role 변경,탈퇴와 달리 tokenVersion은 증가시키지 않음
    public void changePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
        this.tokenVersion++;
    }
}