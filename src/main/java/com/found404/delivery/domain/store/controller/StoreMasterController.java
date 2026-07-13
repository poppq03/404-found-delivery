package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreMasterController {

    private final StoreService storeService;


    // 가게 승인

    // 가게 정지

    // 가게 삭제


}
