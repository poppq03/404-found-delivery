package com.found404.delivery.domain.address.controller;

import com.found404.delivery.domain.address.dto.AddressRequestDto;
import com.found404.delivery.domain.address.dto.AddressResponseDto;
import com.found404.delivery.domain.address.dto.AddressUpdateRequestDto;
import com.found404.delivery.domain.address.service.AddressService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Address", description = "배송지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "배송지 등록", description = "로그인한 고객의 배송지를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponseDto>> createAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequestDto request
    ) {
        AddressResponseDto response = addressService.createAddress(userDetails.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "배송지가 등록되었습니다."));
    }

    @Operation(summary = "내 배송지 목록 조회", description = "로그인한 고객의 배송지 목록을 페이지 단위로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AddressResponseDto>>> getAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기. 10, 30, 50만 허용", example = "10")
            @RequestParam(defaultValue = "10") int size
            ) {
        Page<AddressResponseDto> response = addressService.getMyAddresses(
                userDetails.getUserId(),
                page,
                size
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배송지 단건 조회", description = "로그인한 고객의 배송지 상세 정보를 조회합니다.")
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponseDto>> getAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "배송지 ID", required = true)
            @PathVariable UUID addressId
    ) {
        AddressResponseDto response = addressService.getAddress(
                userDetails.getUserId(),
                addressId
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배송지 수정", description = "로그인한 고객의 배송지 정보를 수정합니다.")
    @PatchMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponseDto>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "배송지 ID", required = true)
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressUpdateRequestDto request
    ) {
        AddressResponseDto response = addressService.updateAddress(
                userDetails.getUserId(),
                addressId,
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response, "배송지가 수정되었습니다."));
    }

    @Operation(summary = "배송지 삭제", description = "로그인한 고객의 배송지를 삭제합니다.")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "배송지 ID", required = true)
            @PathVariable UUID addressId
    ) {
        addressService.deleteAddress(userDetails.getUserId(), addressId);

        return ResponseEntity.ok(ApiResponse.success(null, "배송지가 삭제되었습니다."));
    }

    @Operation(summary = "기본 배송지 설정", description = "로그인한 고객의 특정 배송지를 기본 배송지로 설정합니다.")
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressResponseDto>> setDefaultAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "배송지 ID", required = true)
            @PathVariable UUID addressId
    ) {
        AddressResponseDto response = addressService.setDefaultAddress(
                userDetails.getUserId(),
                addressId
        );

        return ResponseEntity.ok(ApiResponse.success(response, "기본 배송지가 설정되었습니다."));
    }

}
