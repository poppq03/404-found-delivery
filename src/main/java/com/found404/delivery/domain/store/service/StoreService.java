package com.found404.delivery.domain.store.service;

import com.found404.delivery.domain.store.dto.request.StoreCreateRequest;
import com.found404.delivery.domain.store.dto.response.StoreDetailResponse;
import com.found404.delivery.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Service
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreService {

    private final StoreRepository storeRepository;



}
