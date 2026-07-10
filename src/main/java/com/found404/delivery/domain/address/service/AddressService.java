package com.found404.delivery.domain.address.service;

import com.found404.delivery.domain.address.dto.AddressRequestDto;
import com.found404.delivery.domain.address.dto.AddressResponseDto;
import com.found404.delivery.domain.address.dto.AddressUpdateRequestDto;
import com.found404.delivery.domain.address.entity.Address;
import com.found404.delivery.domain.address.repository.AddressRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional
    public AddressResponseDto createAddress(Long userId, @Valid AddressRequestDto request) {
        Address address = Address.create(userId, request);
        Address savedAddress = addressRepository.save(address);

        return AddressResponseDto.from(savedAddress);
    }

    public Page<AddressResponseDto> getMyAddresses(Long userId, int page, int size) {
        validatePageSize(size);

        Pageable pageable = PageRequest.of(page, size);

        return addressRepository.findAllByUserId(userId, pageable)
                .map(AddressResponseDto::from);
    }

    public AddressResponseDto getAddress(Long userId, UUID addressId) {
        Address address = findAddressByIdAndUserId(addressId, userId);
        return AddressResponseDto.from(address);
    }

    @Transactional
    public AddressResponseDto updateAddress(Long userId, UUID addressId, @Valid AddressUpdateRequestDto request) {
        Address address = findAddressByIdAndUserId(addressId, userId);
        address.update(request);

        return AddressResponseDto.from(address);
    }

    @Transactional
    public void deleteAddress(Long userId, UUID addressId) {
        Address address = findAddressByIdAndUserId(addressId, userId);
        address.markDeleted(userId);
    }


    private void validatePageSize(int size) {
        if (size != 10 && size != 30 && size != 50) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }

    private Address findAddressByIdAndUserId(UUID addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ADDRESS);
        }

        return address;
    }

    @Transactional
    public AddressResponseDto setDefaultAddress(Long userId, UUID addressId) {
        Address targetAddress = findAddressByIdAndUserId(addressId, userId);

        addressRepository.findAllByUserIdAndIsDefaultTrue(userId)
                .forEach(Address::unsetDefaultAddress);

        targetAddress.setDefaultAddress();

        return AddressResponseDto.from(targetAddress);
    }
}