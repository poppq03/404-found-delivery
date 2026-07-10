package com.found404.delivery.domain.address.dto;

import com.found404.delivery.domain.address.entity.Address;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AddressResponseDto {

    private final UUID addressId;
    private final String addressName;
    private final String address;
    private final String detailAddress;
    private final String receiverName;
    private final String phone;
    private final boolean isDefault;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AddressResponseDto(UUID addressId, String addressName, String address, String detailAddress, String receiverName, String phone, boolean isDefault, LocalDateTime createdAt, LocalDateTime updatedAt) {
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