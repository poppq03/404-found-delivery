package com.found404.delivery.domain.address.controller;

import com.found404.delivery.domain.address.dto.AddressRequestDto;
import com.found404.delivery.domain.address.dto.AddressResponseDto;
import com.found404.delivery.domain.address.dto.AddressUpdateRequestDto;
import com.found404.delivery.domain.address.service.AddressService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

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
}
