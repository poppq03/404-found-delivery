package com.found404.delivery.domain.store.repository;


import com.found404.delivery.domain.category.entity.Category;
import com.found404.delivery.domain.region.entity.Region;
import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;



@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(StoreRepositoryTest.TestJpaConfig.class)
class StoreRepositoryTest {


    @Autowired
    private StoreRepository storeRepository;


    @Autowired
    private EntityManager entityManager;



    @TestConfiguration
    static class TestJpaConfig {

        @Bean(name = "auditorAwareImpl")
        public AuditorAware<Long> auditorAwareImpl() {

            return () -> Optional.of(1L);
        }
    }



    @Test
    @DisplayName("전체 가게 조회 - OPEN 상태가 먼저 조회된다")
    void findStoreListTest() {


        Store openStore = createStore(
                "열린 가게",
                StoreStatus.OPEN
        );


        Store closedStore = createStore(
                "닫힌 가게",
                StoreStatus.CLOSED
        );


        storeRepository.save(openStore);
        storeRepository.save(closedStore);



        Slice<Store> result =
                storeRepository.findStoreList(
                        PageRequest.of(0,10)
                );



        assertThat(result.getContent())
                .hasSize(2);


        assertThat(
                result.getContent()
                        .get(0)
                        .getStatus()
        )
                .isEqualTo(StoreStatus.OPEN);

    }





    @Test
    @DisplayName("가게 상세 조회")
    void findStoreDetailTest(){


        Store store =
                createStore(
                        "테스트 가게",
                        StoreStatus.OPEN
                );


        Store saved =
                storeRepository.save(store);



        Store result =
                storeRepository
                        .findByStoreIdAndIsActiveTrueAndStatusNot(
                                saved.getStoreId(),
                                StoreStatus.SUSPENDED
                        )
                        .orElseThrow();



        assertThat(result.getName())
                .isEqualTo("테스트 가게");

    }





    @Test
    @DisplayName("삭제된 가게는 조회되지 않는다")
    void findDeletedStoreTest(){


        Store store =
                createStore(
                        "삭제 가게",
                        StoreStatus.OPEN
                );


        store.delete(1L);



        Store saved =
                storeRepository.save(store);



        boolean exists =
                storeRepository
                        .findByStoreIdAndIsActiveTrueAndStatusNot(
                                saved.getStoreId(),
                                StoreStatus.SUSPENDED
                        )
                        .isPresent();



        assertThat(exists)
                .isFalse();

    }






    @Test
    @DisplayName("OWNER 가게 존재 여부 확인")
    void existsOwnerStoreTest(){


        Store store =
                createStore(
                        "사장님 가게",
                        StoreStatus.OPEN
                );


        Store saved =
                storeRepository.save(store);



        boolean result =
                storeRepository.existsByStoreIdAndOwnerId(
                        saved.getStoreId(),
                        store.getOwner().getId()
                );



        assertThat(result)
                .isTrue();

    }






    private Store createStore(
            String name,
            StoreStatus status
    ){


        User user =
                User.create(
                        "testUser",
                        "password",
                        "test@test.com",
                        "테스트",
                        "01012345678",
                        Role.OWNER
                );


        entityManager.persist(user);



        Category category =
                Category.createCategory(
                        "한식",
                        "테스트 카테고리"
                );


        entityManager.persist(category);




        Region region =
                Region.createRegion(
                        "서울"
                );


        entityManager.persist(region);




        return Store.builder()
                .owner(user)
                .category(category)
                .region(region)
                .name(name)
                .phoneNumber("01012345678")
                .address("서울")
                .detailAddress("1층")
                .minOrderPrice(10000)
                .deliveryFee(3000)
                .status(status)
                .isActive(true)
                .imageUrl("test.png")
                .build();

    }

}