package com.found404.delivery.domain.user.repository;

import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import com.found404.delivery.global.config.AuditorAwareImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

// UserRepository의 커스텀 JPQL 쿼리(findTokenVersionById, search)와
// @SQLRestriction("deleted_at IS NULL")이 실제 DB 레벨에서 의도대로 동작하는지 검증하는 통합 테스트.
// Mockito 단위 테스트(UserServiceTest)와 달리, 여기는 진짜 쿼리를 실제 DB에 날려서 검증한다.
// H2 등 임베디드 DB로 자동 대체되지 않도록(우리 프로젝트는 Postgres 전용 문법을 쓰므로) Replace.NONE 지정.
//
// @DataJpaTest는 JPA 관련 빈만 최소로 스캔하는 슬라이스 테스트라 AuditorAwareImpl(@Component)이 빠지는데,
// DeliveryApplication의 @EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")이 정확히
// "auditorAwareImpl"이라는 이름의 빈을 요구한다. @Import(AuditorAwareImpl.class)만으로는
// 빈 이름이 그 값과 정확히 일치하지 않아 여전히 못 찾는 문제가 있어서,
// @TestConfiguration으로 이름을 못 박아 직접 등록한다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(UserRepositoryTest.TestAuditingConfig.class)
class UserRepositoryTest {

    @TestConfiguration
    static class TestAuditingConfig {
        @Bean(name = "auditorAwareImpl") // @EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")가 찾는 그 이름
        public AuditorAware<Long> auditorAwareImpl() {
            return new AuditorAwareImpl();
        }
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User persistUser(String username, String email, String nickname, String phone, Role role) {
        User user = User.create(username, "ENCODED_PW", email, nickname, phone, role);
        return entityManager.persistAndFlush(user);
    }

    // ===== findByUsername =====

    @Test
    @DisplayName("findByUsername 성공 - 존재하는 username이면 조회됨")
    void findByUsername_success() {
        persistUser("repotest1", "repo1@example.com", "레포테스트", "01011110000", Role.CUSTOMER);

        Optional<User> found = userRepository.findByUsername("repotest1");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("repo1@example.com");
    }

    @Test
    @DisplayName("findByUsername 실패 - 존재하지 않으면 empty")
    void findByUsername_fail_notFound() {
        Optional<User> found = userRepository.findByUsername("nobody");

        assertThat(found).isEmpty();
    }

    // ===== existsByUsername / existsByEmail =====

    @Test
    @DisplayName("existsByUsername 성공 - 존재하면 true")
    void existsByUsername_success_true() {
        persistUser("repotest2", "repo2@example.com", "레포테스트2", "01011110001", Role.CUSTOMER);

        assertThat(userRepository.existsByUsername("repotest2")).isTrue();
    }

    @Test
    @DisplayName("existsByUsername 실패(반대 케이스) - 존재하지 않으면 false")
    void existsByUsername_fail_false() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    @DisplayName("existsByUsername - 탈퇴한 계정은 @SQLRestriction에 의해 false (재가입 허용 로직의 근거)")
    void existsByUsername_withdrawnAccount_returnsFalse() {
        User user = persistUser("repotest3", "repo3@example.com", "탈퇴예정", "01011110002", Role.CUSTOMER);

        user.withdraw(user.getId());
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 캐시를 비워서 진짜 DB 쿼리를 타도록 강제

        assertThat(userRepository.existsByUsername("repotest3")).isFalse();
    }

    @Test
    @DisplayName("existsByEmail 성공 - 존재하면 true")
    void existsByEmail_success_true() {
        persistUser("repotest4", "repo4@example.com", "레포테스트4", "01011110003", Role.CUSTOMER);

        assertThat(userRepository.existsByEmail("repo4@example.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail 실패(반대 케이스) - 존재하지 않으면 false")
    void existsByEmail_fail_false() {
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    // ===== findTokenVersionById =====

    @Test
    @DisplayName("findTokenVersionById 성공 - 존재하는 유저면 tokenVersion 반환")
    void findTokenVersionById_success() {
        User user = persistUser("repotest5", "repo5@example.com", "레포테스트5", "01011110004", Role.CUSTOMER);

        Optional<Long> version = userRepository.findTokenVersionById(user.getId());

        assertThat(version).contains(0L);
    }

    @Test
    @DisplayName("findTokenVersionById 실패 - 탈퇴한 유저는 @SQLRestriction에 의해 empty (JwtAuthenticationFilter가 401로 막는 근거)")
    void findTokenVersionById_fail_withdrawnUser() {
        User user = persistUser("repotest6", "repo6@example.com", "레포테스트6", "01011110005", Role.CUSTOMER);
        Long userId = user.getId();

        user.withdraw(userId);
        entityManager.flush();
        entityManager.clear();

        Optional<Long> version = userRepository.findTokenVersionById(userId);

        assertThat(version).isEmpty();
    }

    // ===== search =====

    @Test
    @DisplayName("search 성공 - keyword가 nickname에 매칭되면 조회됨")
    void search_success_matchNickname() {
        persistUser("search1", "search1@example.com", "검색용닉네임", "01022220000", Role.CUSTOMER);
        persistUser("search2", "search2@example.com", "상관없는유저", "01022220001", Role.CUSTOMER);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.search("%검색용%", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("search1");
    }

    @Test
    @DisplayName("search 성공 - keyword가 phone에 매칭되면 조회됨")
    void search_success_matchPhone() {
        persistUser("search3", "search3@example.com", "폰검색", "01099998888", Role.CUSTOMER);

        Page<User> result = userRepository.search("%01099998888%", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("search - role 필터 적용 시 다른 role은 결과에서 제외됨")
    void search_roleFilter_excludesOtherRoles() {
        // 로컬 개발 DB를 그대로 쓰는 테스트라(별도 테스트 전용 DB가 아님), keyword를 "%"(전체 매칭)로 두면
        // 이전에 수동으로 만들어 이미 커밋된(@DataJpaTest 롤백 대상이 아닌) 다른 계정까지 섞여 나올 수 있다.
        // 그래서 이번 테스트에서만 쓰는 고유 마커를 nickname에 심어 검색 범위를 좁힌다.
        persistUser("rfcust1", "rfcustomerA@example.com", "RF마커고객", "01033330000", Role.CUSTOMER);
        persistUser("rfown1", "rfownerA@example.com", "RF마커사장", "01033330001", Role.OWNER);

        Page<User> result = userRepository.search("%RF마커%", Role.CUSTOMER, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(User::getUsername)
                .containsExactly("rfcust1")
                .doesNotContain("rfown1");
    }

    @Test
    @DisplayName("search - 탈퇴한 유저는 @SQLRestriction에 의해 검색 결과에서 제외됨")
    void search_excludesWithdrawnUsers() {
        User user = persistUser("willwdr1", "willwithdraw@example.com", "탈퇴할유저", "01044440000", Role.CUSTOMER);

        user.withdraw(user.getId());
        entityManager.flush();
        entityManager.clear();

        Page<User> result = userRepository.search("%willwdr1%", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }
}