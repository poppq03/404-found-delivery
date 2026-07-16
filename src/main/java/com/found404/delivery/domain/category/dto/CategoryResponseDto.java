package com.found404.delivery.domain.category.dto;

import com.found404.delivery.domain.category.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CategoryResponseDto {

    private UUID categoryId;
    private String name;
    private String description;
    private Boolean isActive;

    public static CategoryResponseDto from(Category category) {
        return CategoryResponseDto.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .build();
    }
}