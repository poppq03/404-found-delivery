package com.found404.delivery.domain.address.dto;

import com.found404.delivery.domain.address.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "배송지 응답 DTO")
@Getter
public class AddressResponseDto {

    @Schema(description = "배송지 ID", example = "11111111-1111-1111-1111-111111111111")
    private final UUID addressId;
    @Schema(description = "주소 별칭", example = "우리집")
    private final String addressName;
    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    private final String address;
    @Schema(description = "상세 주소", example = "101동 1001호")
    private final String detailAddress;
    @Schema(description = "수령인 이름", example = "홍길동")
    private final String receiverName;
    @Schema(description = "수령인 연락처", example = "010-1234-5678")
    private final String phone;
    @Schema(description = "기본 배송지 여부", example = "true")
    private final boolean isDefault;
    @Schema(description = "생성 일시", example = "2026-07-16T10:00:00")
    private final LocalDateTime createdAt;
    @Schema(description = "수정 일시", example = "2026-07-16T10:30:00")
    private final LocalDateTime updatedAt;

    public AddressResponseDto(
            UUID addressId,
            String addressName,
            String address,
            String detailAddress,
            String receiverName,
            String phone,
            boolean isDefault,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.addressId = addressId;
        this.addressName = addressName;
        this.address = address;
        this.detailAddress = detailAddress;
        this.receiverName = receiverName;
        this.phone = phone;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AddressResponseDto from(Address address) {
        return new AddressResponseDto(
                address.getId(),
                address.getAddressName(),
                address.getAddress(),
                address.getDetailAddress(),
                address.getReceiverName(),
                address.getPhone(),
                address.isDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt());
    }
}
