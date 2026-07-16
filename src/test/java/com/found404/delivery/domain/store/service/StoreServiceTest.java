package com.found404.delivery.domain.store.service;

import com.found404.delivery.domain.category.entity.Category;
import com.found404.delivery.domain.category.repository.CategoryRepository;
import com.found404.delivery.domain.region.entity.Region;
import com.found404.delivery.domain.region.repository.RegionRepository;
import com.found404.delivery.domain.store.dto.request.MinOrderPriceUpdateRequestDto;
import com.found404.delivery.domain.store.dto.request.StoreCreateRequestDto;
import com.found404.delivery.domain.store.dto.request.StoreStatusRequestDto;
import com.found404.delivery.domain.store.dto.response.StoreDetailResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponseDto;
import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import com.found404.delivery.domain.store.repository.StoreRepository;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import com.found404.delivery.domain.user.repository.UserRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.storage.ImageStorage;
import com.found404.delivery.global.transaction.AfterCommitExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private ImageStorage imageStorage;

    @Mock
    private AfterCommitExecutor afterCommitExecutor;

    @InjectMocks
    private StoreService storeService;

    private Long ownerId;
    private UUID storeId;
    private UUID categoryId;
    private UUID regionId;

    private User owner;
    private Category category;
    private Region region;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        storeId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        regionId = UUID.randomUUID();

        /*
         * 객체만 생성하고 when()은 넣지 않는다.
         * 각 테스트에서 필요한 동작만 stub 처리한다.
         */
        owner = mock(User.class);
        category = mock(Category.class);
        region = mock(Region.class);
    }

    @Nested
    @DisplayName("가게 등록")
    class CreateStoreTest {

        @Test
        @DisplayName("OWNER는 이미지 없이 가게를 등록할 수 있다")
        void createStoreWithoutImageSuccess() {
            // given
            StoreCreateRequestDto request = createRequestMock();

            when(owner.getRole()).thenReturn(Role.OWNER);
            when(userRepository.findById(ownerId))
                    .thenReturn(Optional.of(owner));
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.of(category));
            when(regionRepository.findById(regionId))
                    .thenReturn(Optional.of(region));

            when(storeRepository.save(any(Store.class)))
                    .thenAnswer(invocation -> {
                        Store store = invocation.getArgument(0);
                        ReflectionTestUtils.setField(
                                store,
                                "storeId",
                                storeId
                        );
                        return store;
                    });

            // when
            StoreDetailResponseDto response =
                    storeService.createStore(
                            ownerId,
                            request,
                            null
                    );

            // then
            assertThat(response).isNotNull();

            ArgumentCaptor<Store> storeCaptor =
                    ArgumentCaptor.forClass(Store.class);

            verify(storeRepository).save(storeCaptor.capture());

            Store savedStore = storeCaptor.getValue();

            assertThat(savedStore.getOwner()).isEqualTo(owner);
            assertThat(savedStore.getCategory()).isEqualTo(category);
            assertThat(savedStore.getRegion()).isEqualTo(region);
            assertThat(savedStore.getName()).isEqualTo("테스트 가게");
            assertThat(savedStore.getStatus())
                    .isEqualTo(StoreStatus.PENDING);
            assertThat(savedStore.getIsActive()).isTrue();

            verify(imageStorage, never())
                    .validateImage(any());
            verify(imageStorage, never())
                    .upload(anyString(), any());
        }

        @Test
        @DisplayName("OWNER는 이미지와 함께 가게를 등록할 수 있다")
        void createStoreWithImageSuccess() {
            // given
            StoreCreateRequestDto request = createRequestMock();

            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "store.png",
                    "image/png",
                    "image-data".getBytes()
            );

            when(owner.getRole()).thenReturn(Role.OWNER);
            when(userRepository.findById(ownerId))
                    .thenReturn(Optional.of(owner));
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.of(category));
            when(regionRepository.findById(regionId))
                    .thenReturn(Optional.of(region));
            when(imageStorage.validateImage(image))
                    .thenReturn("png");

            when(storeRepository.save(any(Store.class)))
                    .thenAnswer(invocation -> {
                        Store store = invocation.getArgument(0);
                        ReflectionTestUtils.setField(
                                store,
                                "storeId",
                                storeId
                        );
                        return store;
                    });

            when(imageStorage.toUrlOrNull(
                    "stores/" + storeId + ".png"
            )).thenReturn(
                    "https://example.com/stores/" + storeId + ".png"
            );

            // when
            StoreDetailResponseDto response =
                    storeService.createStore(
                            ownerId,
                            request,
                            image
                    );

            // then
            assertThat(response).isNotNull();

            verify(imageStorage).validateImage(image);
            verify(imageStorage).upload(
                    "stores/" + storeId + ".png",
                    image
            );
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 가게를 등록할 수 없다")
        void createStoreUserNotFound() {
            // given
            StoreCreateRequestDto request =
                    mock(StoreCreateRequestDto.class);

            when(userRepository.findById(ownerId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> storeService.createStore(
                            ownerId,
                            request,
                            null
                    )
            ).isInstanceOf(CustomException.class);

            verify(categoryRepository, never())
                    .findById(any());
            verify(regionRepository, never())
                    .findById(any());
            verify(storeRepository, never())
                    .save(any());
        }

        @Test
        @DisplayName("OWNER 권한이 아니면 가게를 등록할 수 없다")
        void createStoreForbidden() {
            // given
            StoreCreateRequestDto request =
                    mock(StoreCreateRequestDto.class);

            when(owner.getRole()).thenReturn(Role.MASTER);
            when(userRepository.findById(ownerId))
                    .thenReturn(Optional.of(owner));

            // when & then
            assertThatThrownBy(
                    () -> storeService.createStore(
                            ownerId,
                            request,
                            null
                    )
            ).isInstanceOf(CustomException.class);

            verify(categoryRepository, never())
                    .findById(any());
            verify(regionRepository, never())
                    .findById(any());
            verify(storeRepository, never())
                    .save(any());
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 가게를 등록할 수 없다")
        void createStoreCategoryNotFound() {
            // given
            StoreCreateRequestDto request =
                    mock(StoreCreateRequestDto.class);

            /*
             * 카테고리 조회 전까지 실제로 호출되는 값만 stub 한다.
             */
            when(request.getCategoryId()).thenReturn(categoryId);
            when(owner.getRole()).thenReturn(Role.OWNER);
            when(userRepository.findById(ownerId))
                    .thenReturn(Optional.of(owner));
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> storeService.createStore(
                            ownerId,
                            request,
                            null
                    )
            ).isInstanceOf(CustomException.class);

            verify(regionRepository, never())
                    .findById(any());
            verify(storeRepository, never())
                    .save(any());
        }

        @Test
        @DisplayName("존재하지 않는 지역으로 가게를 등록할 수 없다")
        void createStoreRegionNotFound() {
            // given
            StoreCreateRequestDto request =
                    mock(StoreCreateRequestDto.class);

            when(request.getCategoryId()).thenReturn(categoryId);
            when(request.getRegionId()).thenReturn(regionId);

            when(owner.getRole()).thenReturn(Role.OWNER);
            when(userRepository.findById(ownerId))
                    .thenReturn(Optional.of(owner));
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.of(category));
            when(regionRepository.findById(regionId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> storeService.createStore(
                            ownerId,
                            request,
                            null
                    )
            ).isInstanceOf(CustomException.class);

            verify(storeRepository, never())
                    .save(any());
        }
    }

    @Nested
    @DisplayName("가게 상세 조회")
    class GetStoreDetailTest {

        @Test
        @DisplayName("활성화된 가게 상세 정보를 조회한다")
        void getStoreDetailSuccess() {
            // given
            Store store = createStore(
                    StoreStatus.OPEN,
                    true
            );

            when(storeRepository
                    .findByStoreIdAndIsActiveTrueAndStatusNot(
                            storeId,
                            StoreStatus.SUSPENDED
                    ))
                    .thenReturn(Optional.of(store));

            // when
            StoreDetailResponseDto response =
                    storeService.getStoreDetail(storeId);

            // then
            assertThat(response).isNotNull();

            verify(storeRepository)
                    .findByStoreIdAndIsActiveTrueAndStatusNot(
                            storeId,
                            StoreStatus.SUSPENDED
                    );
        }

        @Test
        @DisplayName("조회 가능한 가게가 없으면 예외가 발생한다")
        void getStoreDetailNotFound() {
            // given
            when(storeRepository
                    .findByStoreIdAndIsActiveTrueAndStatusNot(
                            storeId,
                            StoreStatus.SUSPENDED
                    ))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> storeService.getStoreDetail(storeId)
            ).isInstanceOf(CustomException.class);

            verify(imageStorage, never())
                    .toUrlOrNull(any());
        }
    }

    @Nested
    @DisplayName("영업 상태 변경")
    class UpdateStoreStatusTest {

        @Test
        @DisplayName("가게 소유자는 영업 상태를 변경할 수 있다")
        void updateStoreStatusSuccess() {
            // given
            Store store = createOwnerStore(
                    StoreStatus.CLOSED,
                    true
            );

            StoreStatusRequestDto request =
                    mock(StoreStatusRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));
            when(request.getStatus())
                    .thenReturn(StoreStatus.OPEN);

            // when
            StoreStatusResponseDto response =
                    storeService.updateStoreStatus(
                            ownerId,
                            storeId,
                            request
                    );

            // then
            assertThat(response).isNotNull();
            assertThat(store.getStatus())
                    .isEqualTo(StoreStatus.OPEN);
        }

        @Test
        @DisplayName("현재 상태와 같은 상태로 변경하면 예외가 발생한다")
        void updateStoreStatusSameStatus() {
            // given
            Store store = createOwnerStore(
                    StoreStatus.OPEN,
                    true
            );

            StoreStatusRequestDto request =
                    mock(StoreStatusRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));
            when(request.getStatus())
                    .thenReturn(StoreStatus.OPEN);

            // when & then
            assertThatThrownBy(
                    () -> storeService.updateStoreStatus(
                            ownerId,
                            storeId,
                            request
                    )
            ).isInstanceOf(CustomException.class);

            assertThat(store.getStatus())
                    .isEqualTo(StoreStatus.OPEN);
        }

        @Test
        @DisplayName("가게 소유자가 아니면 상태를 변경할 수 없다")
        void updateStoreStatusNotOwner() {
            // given
            Store store = createStore(
                    StoreStatus.CLOSED,
                    true
            );

            StoreStatusRequestDto request =
                    mock(StoreStatusRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            /*
             * 소유자 검사에서 먼저 실패하므로
             * request.getStatus()는 stub 하지 않는다.
             */
            Long otherUserId = 999L;

            // when & then
            assertThatThrownBy(
                    () -> storeService.updateStoreStatus(
                            otherUserId,
                            storeId,
                            request
                    )
            ).isInstanceOf(CustomException.class);

            verify(request, never()).getStatus();

            assertThat(store.getStatus())
                    .isEqualTo(StoreStatus.CLOSED);
        }

        @Test
        @DisplayName("삭제된 가게의 상태를 변경하면 예외가 발생한다")
        void updateDeletedStoreStatus() {
            // given
            Store store = createStore(
                    StoreStatus.CLOSED,
                    false
            );

            StoreStatusRequestDto request =
                    mock(StoreStatusRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            /*
             * 삭제 여부 검사에서 실패하므로
             * request.getStatus()는 호출되지 않는다.
             */
            // when & then
            assertThatThrownBy(
                    () -> storeService.updateStoreStatus(
                            ownerId,
                            storeId,
                            request
                    )
            ).isInstanceOf(CustomException.class);

            verify(request, never()).getStatus();
        }
    }

    @Nested
    @DisplayName("최소 주문 금액 변경")
    class UpdateMinOrderPriceTest {

        @Test
        @DisplayName("가게 소유자는 최소 주문 금액을 변경할 수 있다")
        void updateMinOrderPriceSuccess() {
            // given
            Store store = createOwnerStore(
                    StoreStatus.OPEN,
                    true
            );

            MinOrderPriceUpdateRequestDto request =
                    mock(MinOrderPriceUpdateRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));
            when(request.getMinOrderPrice())
                    .thenReturn(20_000);

            // when
            StoreStatusResponseDto response =
                    storeService.updateMinOrderPrice(
                            ownerId,
                            storeId,
                            request
                    );

            // then
            assertThat(response).isNotNull();
            assertThat(store.getMinOrderPrice())
                    .isEqualTo(20_000);
        }

        @Test
        @DisplayName("기존 최소 주문 금액과 같으면 예외가 발생한다")
        void updateMinOrderPriceSamePrice() {
            // given
            Store store = createOwnerStore(
                    StoreStatus.OPEN,
                    true
            );

            MinOrderPriceUpdateRequestDto request =
                    mock(MinOrderPriceUpdateRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));
            when(request.getMinOrderPrice())
                    .thenReturn(10_000);

            // when & then
            assertThatThrownBy(
                    () -> storeService.updateMinOrderPrice(
                            ownerId,
                            storeId,
                            request
                    )
            ).isInstanceOf(CustomException.class);

            assertThat(store.getMinOrderPrice())
                    .isEqualTo(10_000);
        }

        @Test
        @DisplayName("소유자가 아니면 최소 주문 금액을 변경할 수 없다")
        void updateMinOrderPriceNotOwner() {
            // given
            Store store = createStore(
                    StoreStatus.OPEN,
                    true
            );

            MinOrderPriceUpdateRequestDto request =
                    mock(MinOrderPriceUpdateRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(
                    () -> storeService.updateMinOrderPrice(
                            999L,
                            storeId,
                            request
                    )
            ).isInstanceOf(CustomException.class);

            verify(request, never()).getMinOrderPrice();
        }
    }

    @Nested
    @DisplayName("가게 삭제")
    class DeleteStoreTest {

        @Test
        @DisplayName("가게 소유자는 가게를 삭제할 수 있다")
        void deleteStoreSuccess() {
            // given
            Store store = createOwnerStore(
                    StoreStatus.OPEN,
                    true
            );

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            // when
            StoreStatusResponseDto response =
                    storeService.deleteStore(
                            ownerId,
                            storeId
                    );

            // then
            assertThat(response).isNotNull();
            assertThat(store.getIsActive()).isFalse();
            assertThat(store.getStatus())
                    .isEqualTo(StoreStatus.SUSPENDED);
        }

        @Test
        @DisplayName("소유자가 아니면 가게를 삭제할 수 없다")
        void deleteStoreNotOwner() {
            // given
            Store store = createStore(
                    StoreStatus.OPEN,
                    true
            );

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(
                    () -> storeService.deleteStore(
                            999L,
                            storeId
                    )
            ).isInstanceOf(CustomException.class);

            assertThat(store.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("이미 삭제된 가게를 다시 삭제하면 예외가 발생한다")
        void deleteStoreAlreadyDeleted() {
            // given
            Store store = createStore(
                    StoreStatus.SUSPENDED,
                    false
            );

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(
                    () -> storeService.deleteStore(
                            ownerId,
                            storeId
                    )
            ).isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("관리자 가게 승인")
    class StoreApprovalTest {

        @Test
        @DisplayName("승인 대기 가게를 승인하면 OPEN 상태가 된다")
        void storeApprovalSuccess() {
            // given
            Store store = createStore(
                    StoreStatus.PENDING,
                    true
            );

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            // when
            StoreStatusResponseDto response =
                    storeService.storeApproval(
                            100L,
                            storeId
                    );

            // then
            assertThat(response).isNotNull();
            assertThat(store.getStatus())
                    .isEqualTo(StoreStatus.OPEN);
        }

        @Test
        @DisplayName("이미 승인된 가게는 다시 승인할 수 없다")
        void storeApprovalAlreadyApproved() {
            // given
            Store store = createStore(
                    StoreStatus.OPEN,
                    true
            );

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(
                    () -> storeService.storeApproval(
                            100L,
                            storeId
                    )
            ).isInstanceOf(CustomException.class);

            assertThat(store.getStatus())
                    .isEqualTo(StoreStatus.OPEN);
        }

        @Test
        @DisplayName("정지된 가게는 승인할 수 없다")
        void storeApprovalSuspended() {
            // given
            Store store = createStore(
                    StoreStatus.SUSPENDED,
                    true
            );

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(
                    () -> storeService.storeApproval(
                            100L,
                            storeId
                    )
            ).isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("삭제된 가게는 승인할 수 없다")
        void storeApprovalDeletedStore() {
            // given
            Store store = createStore(
                    StoreStatus.PENDING,
                    false
            );

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(
                    () -> storeService.storeApproval(
                            100L,
                            storeId
                    )
            ).isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("관리자 가게 상태 변경")
    class UpdateStoreStatusByMasterTest {

        @Test
        @DisplayName("관리자는 가게 상태를 변경할 수 있다")
        void updateStatusByMasterSuccess() {
            // given
            Store store = createStore(
                    StoreStatus.OPEN,
                    true
            );

            StoreStatusRequestDto request =
                    mock(StoreStatusRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));
            when(request.getStatus())
                    .thenReturn(StoreStatus.SUSPENDED);

            // when
            StoreStatusResponseDto response =
                    storeService.updateStoreStatusByMaster(
                            100L,
                            storeId,
                            request
                    );

            // then
            assertThat(response).isNotNull();
            assertThat(store.getStatus())
                    .isEqualTo(StoreStatus.SUSPENDED);
        }

        @Test
        @DisplayName("관리자는 가게 상태를 PENDING으로 변경할 수 없다")
        void updateStatusByMasterPendingFail() {
            // given
            Store store = createStore(
                    StoreStatus.OPEN,
                    true
            );

            StoreStatusRequestDto request =
                    mock(StoreStatusRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));
            when(request.getStatus())
                    .thenReturn(StoreStatus.PENDING);

            // when & then
            assertThatThrownBy(
                    () -> storeService.updateStoreStatusByMaster(
                            100L,
                            storeId,
                            request
                    )
            ).isInstanceOf(CustomException.class);

            assertThat(store.getStatus())
                    .isEqualTo(StoreStatus.OPEN);
        }

        @Test
        @DisplayName("관리자가 동일한 상태로 변경하면 예외가 발생한다")
        void updateStatusByMasterSameStatus() {
            // given
            Store store = createStore(
                    StoreStatus.OPEN,
                    true
            );

            StoreStatusRequestDto request =
                    mock(StoreStatusRequestDto.class);

            when(storeRepository.findById(storeId))
                    .thenReturn(Optional.of(store));
            when(request.getStatus())
                    .thenReturn(StoreStatus.OPEN);

            // when & then
            assertThatThrownBy(
                    () -> storeService.updateStoreStatusByMaster(
                            100L,
                            storeId,
                            request
                    )
            ).isInstanceOf(CustomException.class);
        }
    }

    /*
     * 성공 테스트에서만 사용하는 전체 요청 Mock.
     * 이 메서드를 예외 테스트에 사용하면 일부 getter가 호출되지 않아
     * UnnecessaryStubbingException이 발생할 수 있다.
     */
    private StoreCreateRequestDto createRequestMock() {
        StoreCreateRequestDto request =
                mock(StoreCreateRequestDto.class);

        when(request.getCategoryId()).thenReturn(categoryId);
        when(request.getRegionId()).thenReturn(regionId);
        when(request.getName()).thenReturn("테스트 가게");
        when(request.getPhoneNumber()).thenReturn("02-1234-5678");
        when(request.getDescription()).thenReturn("테스트 설명");
        when(request.getAddress()).thenReturn("서울특별시 강남구");
        when(request.getDetailAddress()).thenReturn("101호");
        when(request.getMinOrderPrice()).thenReturn(10_000);
        when(request.getDeliveryFee()).thenReturn(3_000);

        return request;
    }

    private Store createStore(
            StoreStatus status,
            boolean isActive
    ) {

        Store store = Store.builder()
                .owner(owner)
                .category(category)
                .region(region)
                .name("테스트 가게")
                .phoneNumber("02-1234-5678")
                .description("테스트 설명")
                .address("서울특별시 강남구")
                .detailAddress("101호")
                .minOrderPrice(10_000)
                .deliveryFee(3_000)
                .imageUrl(null)
                .status(status)
                .isActive(isActive)
                .build();

        ReflectionTestUtils.setField(
                store,
                "storeId",
                storeId
        );

        return store;
    }

    private Store createOwnerStore(
            StoreStatus status,
            boolean isActive
    ) {

        when(owner.getId()).thenReturn(ownerId);

        Store store = Store.builder()
                .owner(owner)
                .category(category)
                .region(region)
                .name("테스트 가게")
                .phoneNumber("02-1234-5678")
                .description("테스트 설명")
                .address("서울특별시 강남구")
                .detailAddress("101호")
                .minOrderPrice(10_000)
                .deliveryFee(3_000)
                .imageUrl(null)
                .status(status)
                .isActive(isActive)
                .build();

        ReflectionTestUtils.setField(
                store,
                "storeId",
                storeId
        );

        return store;
    }
}