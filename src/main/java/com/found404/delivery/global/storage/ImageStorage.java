package com.found404.delivery.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

    void upload(String key, MultipartFile file);

    void delete(String key);

    String toUrl(String key);
}
