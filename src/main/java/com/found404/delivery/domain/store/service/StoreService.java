package com.found404.delivery.domain.store.service;

import com.found404.delivery.domain.category.entity.Category;
import com.found404.delivery.domain.category.repository.CategoryRepository;
import com.found404.delivery.domain.region.entity.Region;
import com.found404.delivery.domain.region.repository.RegionRepository;
import com.found404.delivery.domain.store.dto.request.MinOrderPriceUpdateRequestDto;
import com.found404.delivery.domain.store.dto.request.StoreCreateRequestDto;
import com.found404.delivery.domain.store.dto.request.StoreStatusRequestDto;
import com.found404.delivery.domain.store.dto.request.StoreUpdateRequestDto;
import com.found404.delivery.domain.store.dto.response.StoreDetailResponseDto;
import com.found404.delivery.domain.store.dto.response.StorePendingResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreSimpleResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponseDto;
import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import com.found404.delivery.domain.store.repository.StoreRepository;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import com.found404.delivery.domain.user.repository.UserRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import com.found404.delivery.global.storage.ImageStorage;
import com.found404.delivery.global.transaction.AfterCommitExecutor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;
    private final ImageStorage imageStorage;
    private final AfterCommitExecutor afterCommitExecutor;

    // ================================ All ==================================== //
    // 가게목록 조회
    public Slice<StoreSimpleResponseDto> getStores(Pageable pageable) {
        Slice<Store> stores = storeRepository.findStoreList(pageable);
        return stores.map(s -> StoreSimpleResponseDto.from(s, resolveImageUrl(s)));
    }

    // 카테고리 별 가게 목록 조회
    @Transactional(readOnly = true)
    public Slice<StoreSimpleResponseDto> getStoresByCategory(UUID categoryId, Pageable pageable) {
        Slice<Store> stores = storeRepository.findStoreListByCategory(categoryId, StoreStatus.SUSPENDED, pageable);
        return stores.map(s -> StoreSimpleResponseDto.from(s, resolveImageUrl(s)));
    }

    // 키워드 목록 조회
    @Transactional(readOnly = true)
    public Slice<StoreSimpleResponseDto> searchStoresByKeyword(String keyword, Pageable pageable) {
        return storeRepository.searchStores(keyword, StoreStatus.SUSPENDED, pageable)
                .map(s -> StoreSimpleResponseDto.from(s, resolveImageUrl(s)));
    }

    // 가게 세부사항 조회
    @Transactional(readOnly = true)
    public StoreDetailResponseDto getStoreDetail(UUID storeId) {
        Store store = storeRepository.findByStoreIdAndIsActiveTrueAndStatusNot(storeId, StoreStatus.SUSPENDED)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        return StoreDetailResponseDto.from(store, resolveImageUrl(store));
    }

    // ================================ OWNER ================================== //

    // 가게 등록
    @Transactional
    public StoreDetailResponseDto createStore(Long userId, StoreCreateRequestDto request, MultipartFile image) {
        // 유저가 존재하는지 확인
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (owner.getRole() != Role.OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        // 카테고리 조회
        Category category = getCategory(request.getCategoryId());
        // 지역 조회
        Region region = getRegion(request.getRegionId());

        // save 전에 검증 (확장자만 받아둠)
        String ext = null;
        if (image != null && !image.isEmpty()) {
            ext = imageStorage.validateImage(image);
        }

        // Store Entity 생성
        Store store = Store.builder()
                .owner(owner)
                .category(category)
                .region(region)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .description(request.getDescription())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .minOrderPrice(request.getMinOrderPrice())
                .deliveryFee(request.getDeliveryFee())
                .imageUrl(null) // save 후 dirty checking으로 key 채움
                .status(StoreStatus.PENDING)
                .isActive(true)
                .build();

        // 저장
        Store saveStore = storeRepository.save(store);

        // image key 조립
        if (ext != null) {
            String key = storeImageKey(saveStore.getStoreId(), ext);
            imageStorage.upload(key, image);
            saveStore.changeImage(key);
        }

        // 응답 반환
        return StoreDetailResponseDto.from(saveStore, resolveImageUrl(saveStore));
    }

    // 가게 수정
    @Transactional
    public StoreDetailResponseDto updateStore(Long userId, UUID storeId, MultipartFile image, @Valid StoreUpdateRequestDto request) {
        // 가게 조회
        Store store = getStore(storeId);
        // 소유자 확인
        checkIdentification(userId, store);
        // 카테고리 변경
        Category category = getCategory(request.getCategoryId());
        // 지역 변경
        Region region = getRegion(request.getRegionId());

        store.update(
                request.getName(), request.getPhoneNumber(), request.getDescription(),
                request.getAddress(), request.getDetailAddress(),
                request.getMinOrderPrice(), request.getDeliveryFee(), category, region
        );

        if (image != null && !image.isEmpty()) {
            String ext = imageStorage.validateImage(image);
            String newKey = storeImageKey(store.getStoreId(), ext);
            String oldKey = store.getImageUrl();

            imageStorage.upload(newKey, image);
            store.changeImage(newKey);

            // 트랜잭션 커밋 후 기존 이미지 삭제 (확장자가 바뀐 경우)
            if (oldKey != null && !oldKey.equals(newKey)) {
                afterCommitExecutor.execute(() -> imageStorage.delete(oldKey));
            }
        }

        return StoreDetailResponseDto.from(store, resolveImageUrl(store));
    }

    // 가게 삭제
    @Transactional
    public StoreStatusResponseDto deleteStore(Long userId, UUID storeId) {
        // 가게 조회
        Store store = getStore(storeId);
        // 소유자 확인
        checkIdentification(userId, store);
        // 이미 삭제되었는지 확인
        checkDeletedStore(store);

        store.delete(userId); // soft delete, S3 파일도 삭제되지 않음

        return new StoreStatusResponseDto(storeId, StoreStatus.SUSPENDED, "성공적으로 삭제되었습니다.");
    }

    // 스토어 영업상태 변경
    @Transactional
    public StoreStatusResponseDto updateStoreStatus(Long userId, UUID storeId, StoreStatusRequestDto request) {
        // 가게 조회
        Store store = getStore(storeId);
        // 소유자 확인
        checkIdentification(userId, store);
        // 삭제된 가게인지 확인
        checkDeletedStore(store);
        // 동일한 상태인지 확인
        if (store.getStatus() == request.getStatus()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        store.changeStatus(request.getStatus());
        return new StoreStatusResponseDto(store.getStoreId(), store.getStatus(), "영업상태가 변경되었습니다.");
    }

    // 최소 주문금액 수정
    @Transactional
    public StoreStatusResponseDto updateMinOrderPrice(Long userId, UUID storeId, MinOrderPriceUpdateRequestDto request) {
        // 가게 조회
        Store store = getStore(storeId);
        // 소유자 확인
        checkIdentification(userId, store);
        // 삭제된 가게인지
        checkDeletedStore(store);
        // 기존 금액과 동일한지
        if (store.getMinOrderPrice().equals(request.getMinOrderPrice())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        // 최소 주문 금액 변경
        store.changeMinOrderPrice(request.getMinOrderPrice());
        return new StoreStatusResponseDto(store.getStoreId(), store.getStatus(), "최소 주문 금액이 변경되었습니다.");
    }

    // ================================ MASTER | MANAGER ================================== //

    // 가게 승인 대기 목록
    @Transactional(readOnly = true)
    public Slice<StorePendingResponseDto> getPendingStores(Pageable pageable) {
        return storeRepository.findByStatusAndIsActiveTrue(StoreStatus.PENDING, pageable)
                .map(StorePendingResponseDto::from);
    }

    // 가게 승인 API
    @Transactional
    public StoreStatusResponseDto storeApproval(Long userId, UUID storeId) {
        Store store = getStore(storeId);

        if (!store.getIsActive()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        // 이미 승인된 경우
        if (store.getStatus() == StoreStatus.OPEN || store.getStatus() == StoreStatus.CLOSED
                || store.getStatus() == StoreStatus.BREAK_TIME) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        // 정지된 가게는 승인 불가
        if (store.getStatus() == StoreStatus.SUSPENDED) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        store.approve();
        return new StoreStatusResponseDto(store.getStoreId(), store.getStatus(), "가게 승인이 완료되었습니다.");
    }

    // 가게 상태 변경 API
    @Transactional
    public StoreStatusResponseDto updateStoreStatusByMaster(Long userId, UUID storeId, StoreStatusRequestDto request) {
        Store store = getStore(storeId);
        checkDeletedStore(store);
        StoreStatus newStatus = request.getStatus();
        if (store.getStatus() == newStatus) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (newStatus == StoreStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        store.changeStatus(newStatus);
        return new StoreStatusResponseDto(store.getStoreId(), store.getStatus(), "관리자에 의해 가게 상태가 변경되었습니다.");
    }

    // 가게 삭제 API
    @Transactional
    public StoreStatusResponseDto deleteStoreByMaster(Long userId, UUID storeId) {
        Store store = getStore(storeId);
        checkDeletedStore(store);

        store.delete(userId); // soft delete, S3 파일도 삭제되지 않음

        return new StoreStatusResponseDto(store.getStoreId(), store.getStatus(), "관리자에 의해 가게가 삭제되었습니다.");
    }

    // ================================ 헬퍼 ================================== //

    // 삭제된 가게인지 확인
    private static void checkDeletedStore(Store store) {
        if (Boolean.FALSE.equals(store.getIsActive())) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }
    }

    // 소유자 확인
    private static void checkIdentification(Long userId, Store store) {
        if (!store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_STORE_OWNER);
        }
    }

    // 가게 이미지 S3 key
    private String storeImageKey(UUID storeId, String ext) {
        return "stores/" + storeId + "." + ext;
    }

    // DB에 저장된 S3 key → URL 변환, 이미지 없을 시 null
    private String resolveImageUrl(Store store) {
        return imageStorage.toUrlOrNull(store.getImageUrl());
    }

    @NonNull
    private Region getRegion(UUID request) {
        return regionRepository.findById(request)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));
    }

    @NonNull
    private Category getCategory(UUID request) {
        return categoryRepository.findById(request)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));
    }

    @NonNull
    private Store getStore(UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }
}