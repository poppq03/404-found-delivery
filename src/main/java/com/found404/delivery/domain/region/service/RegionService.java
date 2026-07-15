package com.found404.delivery.domain.region.service;

import com.found404.delivery.domain.region.dto.RegionCreateRequestDto;
import com.found404.delivery.domain.region.dto.RegionResponseDto;
import com.found404.delivery.domain.region.entity.Region;
import com.found404.delivery.domain.region.repository.RegionRepository;
import com.found404.delivery.domain.store.repository.StoreRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {
    private final RegionRepository regionRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public RegionResponseDto createRegion(RegionCreateRequestDto request) {
        // 띄어쓰기 차이로 서로 다른지역으로 저장되는걸 방지
        String regionName = request.getName().trim();

        if(regionRepository.existsByName(regionName)){
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        Region region = Region.createRegion(regionName);
        Region saveRegion = regionRepository.save(region);

        return RegionResponseDto.from(saveRegion);
    }

    @Transactional
    public RegionResponseDto deleteRegion(Long userId, UUID regionId) {
        Region region = getRegion(regionId);

        // 사용중인 지역인지
        if(storeRepository.existsByRegion_RegionIdAndIsActiveTrue(regionId)){
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        region.delete(userId);
        return RegionResponseDto.from(region);

    }

    private Region getRegion(UUID regionId) {
        return regionRepository.findByRegionIdAndIsActiveTrue(regionId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));
    }

}
