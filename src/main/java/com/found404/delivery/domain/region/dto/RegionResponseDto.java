package com.found404.delivery.domain.region.dto;

import com.found404.delivery.domain.region.entity.Region;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RegionResponseDto {

    private UUID regionId;
    private String name;
    private Boolean isActive;




    public static RegionResponseDto from(Region region) {
        return RegionResponseDto.builder()
                .regionId(region.getRegionId())
                .name(region.getName())
                .isActive(region.getIsActive())
                .build();
    }
}