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
import com.found404.delivery.global.storage.S3ImageStorage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;
    private final S3ImageStorage imageStorage;

    // ================================ All ==================================== //
    // 가게목록 조회
    public Slice<StoreSimpleResponseDto> getStores(Pageable pageable) {
        Slice<Store> stores = storeRepository.findStoreList(pageable);
        return stores.map(StoreSimpleResponseDto::from);
    }

    // 카테고리 별 가게 목록 조회
    @Transactional(readOnly = true)
    public Slice<StoreSimpleResponseDto> getStoresByCategory(UUID categoryId, Pageable pageable) {
        Slice<Store> stores = storeRepository
                .findStoreListByCategory(
                        categoryId,
                        StoreStatus.SUSPENDED,
                        pageable
                );
        return stores.map(StoreSimpleResponseDto::from);
    }


    // 키워드 목록 조회
    @Transactional(readOnly = true)
    public Slice<StoreSimpleResponseDto> searchStoresByKeyword(String keyword, Pageable pageable) {
        return storeRepository.searchStores(
                keyword,
                StoreStatus.SUSPENDED,
                pageable
        ).map(StoreSimpleResponseDto::from);
    }


    // 가게 세부사항 조회
    @Transactional(readOnly = true)
    public StoreDetailResponseDto getStoreDetail(UUID storeId) {

        Store store = storeRepository.findByStoreIdAndIsActiveTrueAndStatusNot(
                storeId,
                StoreStatus.SUSPENDED
        ).orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        return StoreDetailResponseDto.from(store);
    }


    // ================================ OWNER ================================== //


    // 가게 등록
    @Transactional
    public StoreDetailResponseDto createStore(Long userId, StoreCreateRequestDto request, MultipartFile image) {
        // 유저가 존재하는지 확인
        User owner = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (owner.getRole() != Role.OWNER){
            throw new CustomException(ErrorCode.FORBIDDEN_ROLE);
        }
        // 카테고리 조회
        Category category = getCategory(request.getCategoryId());
        // 지역 조회
        Region region = getRegion(request.getRegionId());
        String imageUrl="";
        String extension = "";
        String key = "";
        try {
            // 이미지 업로드
            if (image != null && !image.isEmpty()) {
                //확장자, 크기 검증
                extension = imageStorage.validateImage(image);

                 key = "stores/" + UUID.randomUUID() + "." + extension;
                // 이미지 저장
                imageStorage.upload(key, image);

                imageUrl = imageStorage.toUrl(key);
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
                    .imageUrl(imageUrl)
                    .status(StoreStatus.PENDING)
                    .isActive(true)
                    .build();

            // 저장
            Store saveStore = storeRepository.save(store);

            // 응답 반환
            return StoreDetailResponseDto.from(saveStore);

        }catch (Exception e) {
            if(key != null){
                imageStorage.delete(key);
            }
        }throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);

    }



    // 가게 수정
    @Transactional
    public StoreDetailResponseDto updateStore(
            Long userId,
            UUID storeId,
            MultipartFile image,
            @Valid StoreUpdateRequestDto request) {

        // 가게 조회
        Store store = getStore(storeId);

        // 소유자 확인
        checkIdentification(userId, store);
        // 카테고리 변경
        Category category = getCategory(request.getCategoryId());
        // 지역 변경
        Region region = getRegion(request.getRegionId());

        // 이미지 변경
        String imageUrl = store.getImageUrl();
        if(image != null && !image.isEmpty()) {
            String extension = imageStorage.validateImage(image);

            String key = "stores/" + UUID.randomUUID() + "." + extension;
            imageStorage.upload(key, image);
            imageUrl = imageStorage.toUrl(key);
        }

        store.update(
                request.getName(),
                request.getPhoneNumber(),
                request.getDescription(),
                request.getAddress(),
                request.getDetailAddress(),
                request.getMinOrderPrice(),
                request.getDeliveryFee(),
                category,
                region
        );

        store.changeImage(imageUrl);

        return StoreDetailResponseDto.from(store);
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

        store.delete(userId);
        imageStorage.delete(store.getImageUrl());

        return new StoreStatusResponseDto(storeId,StoreStatus.SUSPENDED,"성공적으로 삭제되었습니다.");
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
        if (store.getStatus() == request.getStatus()){
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        store.changeStatus(request.getStatus());
        return new StoreStatusResponseDto(
                store.getStoreId(),
                store.getStatus(),
                "영업상태가 변경되었습니다."
        );
    }

    // 최소 주문금액 수정
    @Transactional
    public StoreStatusResponseDto updateMinOrderPrice(
            Long userId,
            UUID storeId,
            MinOrderPriceUpdateRequestDto request
    ){
        // 가게 조회 
        Store store = getStore(storeId);
        // 소유자 확인
        checkIdentification(userId, store);
        // 삭제된 가게인지
        checkDeletedStore(store);
        // 기존 금액과 동일한지
        if(store.getMinOrderPrice().equals(request.getMinOrderPrice())){
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        // 최소 주문 금액 변경
        store.changeMinOrderPrice(request.getMinOrderPrice());


        return new StoreStatusResponseDto(
                store.getStoreId(),
                store.getStatus(),
                "최소 주문 금액이 변경되었습니다."
        );
    }

    // ================================ MASTER | MANAGER ================================== //



    // 가게 승인 대기 목록
    @Transactional(readOnly = true)
    public Slice<StorePendingResponseDto> getPendingStores(Pageable pageable) {
        return storeRepository.findByStatusAndIsActiveTrue(
                StoreStatus.PENDING,
                pageable
        ).map(StorePendingResponseDto::from);
    }

    // 가게 승인 API
    @Transactional
    public StoreStatusResponseDto storeApproval(Long userId, UUID storeId) {
        Store store = getStore(storeId);

        if(!store.getIsActive()){
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        // 이미 승인된 경우
        if(store.getStatus() == StoreStatus.OPEN || store.getStatus() == StoreStatus.CLOSED
        || store.getStatus() == StoreStatus.BREAK_TIME){
            throw  new CustomException(ErrorCode.INVALID_INPUT);
        }

        // 정지된 가게는 승인 불가
        if(store.getStatus() == StoreStatus.SUSPENDED){
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        store.approve();

        return new StoreStatusResponseDto(
                store.getStoreId(),
                store.getStatus(),
                "가게 승인이 완료되었습니다."
        );


    }

    // 가게 상태 변경 API











    // 삭제된 가게인지 확인
    private static void checkDeletedStore(Store store) {
        if(Boolean.FALSE.equals(store.getIsActive())){
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }
    }

    // 소유자 확인
    private static void checkIdentification(Long userId, Store store) {
        if(!store.getOwner().getId().equals(userId)){
            System.out.println("소유자확인 에러");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }


    @NonNull
    private Region getRegion(UUID request) {
        Region region = regionRepository.findById(request).orElseThrow(() ->
                new CustomException(ErrorCode.INVALID_INPUT));
        return region;
    }

    @NonNull
    private Category getCategory(UUID request) {
        Category category = categoryRepository.findById(request).orElseThrow(() ->
                new CustomException(ErrorCode.INVALID_INPUT));
        return category;
    }

    @NonNull
    private Store getStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        return store;
    }



}
