package com.found404.delivery.domain.address;

import com.found404.delivery.domain.address.dto.AddressRequestDto;
import com.found404.delivery.domain.address.dto.AddressResponseDto;
import com.found404.delivery.domain.address.dto.AddressUpdateRequestDto;
import com.found404.delivery.domain.address.entity.Address;
import com.found404.delivery.domain.address.repository.AddressRepository;
import com.found404.delivery.domain.address.service.AddressService;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    @DisplayName("배송지를 등록한다")
    void createAddress() {
        Long userId = 1L;
        UUID addressId = UUID.randomUUID();
        AddressRequestDto request = addressRequest("집", "서울시 강남구", "101동 1001호", "홍길동", "010-1234-5678");

        given(addressRepository.save(any(Address.class)))
                .willAnswer(invocation -> {
                    Address address = invocation.getArgument(0);
                    ReflectionTestUtils.setField(address, "id", addressId);
                    return address;
                });

        AddressResponseDto response = addressService.createAddress(userId, request);

        assertThat(response.getAddressId()).isEqualTo(addressId);
        assertThat(response.getAddressName()).isEqualTo("집");
        assertThat(response.getAddress()).isEqualTo("서울시 강남구");
        assertThat(response.getReceiverName()).isEqualTo("홍길동");
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("내 배송지 목록을 페이지로 조회한다")
    void getMyAddresses() {
        Long userId = 1L;
        Address address = address(userId, UUID.randomUUID(), "집");

        given(addressRepository.findAllByUserId(eq(userId), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(address)));

        Page<AddressResponseDto> response = addressService.getMyAddresses(userId, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getAddressName()).isEqualTo("집");
    }

    @Test
    @DisplayName("허용되지 않은 페이지 크기면 예외가 발생한다")
    void getMyAddressesInvalidPageSize() {
        assertThatThrownBy(() -> addressService.getMyAddresses(1L, 0, 11))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("본인 배송지가 아니면 단건 조회할 수 없다")
    void getAddressForbidden() {
        UUID addressId = UUID.randomUUID();
        Address address = address(2L, addressId, "회사");

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.getAddress(1L, addressId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_ADDRESS);
    }

    @Test
    @DisplayName("배송지를 수정한다")
    void updateAddress() {
        Long userId = 1L;
        UUID addressId = UUID.randomUUID();
        Address address = address(userId, addressId, "집");
        AddressUpdateRequestDto request = addressUpdateRequest("회사", "서울시 서초구", "202호", "김철수", "010-9999-8888");

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        AddressResponseDto response = addressService.updateAddress(userId, addressId, request);

        assertThat(response.getAddressName()).isEqualTo("회사");
        assertThat(response.getAddress()).isEqualTo("서울시 서초구");
        assertThat(response.getPhone()).isEqualTo("010-9999-8888");
    }

    @Test
    @DisplayName("기본 배송지를 설정하면 기존 기본 배송지는 해제된다")
    void setDefaultAddress() {
        Long userId = 1L;
        UUID targetAddressId = UUID.randomUUID();
        Address currentDefaultAddress = address(userId, UUID.randomUUID(), "집");
        Address targetAddress = address(userId, targetAddressId, "회사");
        currentDefaultAddress.setDefaultAddress();

        given(addressRepository.findById(targetAddressId)).willReturn(Optional.of(targetAddress));
        given(addressRepository.findAllByUserIdAndIsDefaultTrue(userId))
                .willReturn(List.of(currentDefaultAddress));

        AddressResponseDto response = addressService.setDefaultAddress(userId, targetAddressId);

        assertThat(response.isDefault()).isTrue();
        assertThat(targetAddress.isDefault()).isTrue();
        assertThat(currentDefaultAddress.isDefault()).isFalse();
    }

    private Address address(Long userId, UUID addressId, String addressName) {
        AddressRequestDto request = addressRequest(addressName, "서울시 강남구", "101동", "홍길동", "010-1234-5678");
        Address address = Address.create(userId, request);
        ReflectionTestUtils.setField(address, "id", addressId);
        return address;
    }

    private AddressRequestDto addressRequest(String addressName, String address, String detailAddress, String receiverName, String phone) {
        AddressRequestDto request = new AddressRequestDto();
        ReflectionTestUtils.setField(request, "addressName", addressName);
        ReflectionTestUtils.setField(request, "address", address);
        ReflectionTestUtils.setField(request, "detailAddress", detailAddress);
        ReflectionTestUtils.setField(request, "receiverName", receiverName);
        ReflectionTestUtils.setField(request, "phone", phone);
        return request;
    }

    private AddressUpdateRequestDto addressUpdateRequest(String addressName, String address, String detailAddress, String receiverName, String phone) {
        AddressUpdateRequestDto request = new AddressUpdateRequestDto();
        ReflectionTestUtils.setField(request, "addressName", addressName);
        ReflectionTestUtils.setField(request, "address", address);
        ReflectionTestUtils.setField(request, "detailAddress", detailAddress);
        ReflectionTestUtils.setField(request, "receiverName", receiverName);
        ReflectionTestUtils.setField(request, "phone", phone);
        return request;
    }
}
