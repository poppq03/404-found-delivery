package com.found404.delivery.domain.region.service;

import com.found404.delivery.domain.region.dto.RegionCreateRequestDto;
import com.found404.delivery.domain.region.dto.RegionResponseDto;
import com.found404.delivery.domain.region.dto.RegionUpdateRequestDto;
import com.found404.delivery.domain.region.entity.Region;
import com.found404.delivery.domain.region.repository.RegionRepository;
import com.found404.delivery.domain.store.repository.StoreRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        if (regionRepository.existsByName(regionName)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        Region region = Region.createRegion(regionName);
        Region saveRegion = regionRepository.save(region);

        return RegionResponseDto.from(saveRegion);
    }

    public RegionResponseDto getRegion(UUID regionId) {
        return RegionResponseDto.from(getActiveRegion(regionId));
    }

    public Page<RegionResponseDto> searchRegions(String name, Pageable pageable) {
        Page<Region> regions = (name == null || name.isBlank())
                ? regionRepository.findAllByIsActiveTrue(pageable)
                : regionRepository.findAllByIsActiveTrueAndNameContaining(name.trim(), pageable);

        return regions.map(RegionResponseDto::from);
    }

    @Transactional
    public RegionResponseDto updateRegion(UUID regionId, RegionUpdateRequestDto request) {
        Region region = getActiveRegion(regionId);

        String newName = request.getName().trim();
        if (!region.getName().equals(newName) && regionRepository.existsByName(newName)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        region.update(newName);
        return RegionResponseDto.from(region);
    }

    @Transactional
    public RegionResponseDto deleteRegion(Long userId, UUID regionId) {
        Region region = getActiveRegion(regionId);

        // 사용중인 지역인지
        if (storeRepository.existsByRegion_RegionIdAndIsActiveTrue(regionId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        region.delete(userId);
        return RegionResponseDto.from(region);
    }

    private Region getActiveRegion(UUID regionId) {
        return regionRepository.findByRegionIdAndIsActiveTrue(regionId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));
    }
}