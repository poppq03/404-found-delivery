package com.found404.delivery.global.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 용도: 이미지 저장소 공통 인터페이스
 * 1. DB에는 전체 URL이 아닌 S3 key만 저장되고 응답 시 toUrl로 변환해서 내립니다
 * 2. key 접두사는 도메인 구분 및 버킷 폴더 구분입니다 (예: menus/, stores/, users/)
 */

public interface ImageStorage {

    String validateImage(MultipartFile file);

    void upload(String key, MultipartFile file);

    void delete(String key);

    String toUrl(String key);

    // 이미지 없는 경우 response
    default String toUrlOrNull(String key) {
        return key != null ? toUrl(key) : null;
    }
}
