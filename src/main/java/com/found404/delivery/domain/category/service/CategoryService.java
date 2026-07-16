package com.found404.delivery.domain.category.service;

import com.found404.delivery.domain.category.dto.CategoryCreateRequestDto;
import com.found404.delivery.domain.category.dto.CategoryResponseDto;
import com.found404.delivery.domain.category.dto.CategoryUpdateRequestDto;
import com.found404.delivery.domain.category.entity.Category;
import com.found404.delivery.domain.category.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public CategoryResponseDto createCategory(CategoryCreateRequestDto request) {
        String name = request.getName().trim();

        if (categoryRepository.existsByName(name)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        Category category = Category.create(name, request.getDescription());
        Category saved = categoryRepository.save(category);

        return CategoryResponseDto.from(saved);
    }

    public CategoryResponseDto getCategory(UUID categoryId) {
        return CategoryResponseDto.from(getActiveCategory(categoryId));
    }

    public Page<CategoryResponseDto> searchCategories(String name, Pageable pageable) {
        Page<Category> categories = (name == null || name.isBlank())
                ? categoryRepository.findAllByIsActiveTrue(pageable)
                : categoryRepository.findAllByIsActiveTrueAndNameContaining(name.trim(), pageable);

        return categories.map(CategoryResponseDto::from);
    }

    @Transactional
    public CategoryResponseDto updateCategory(UUID categoryId, CategoryUpdateRequestDto request) {
        Category category = getActiveCategory(categoryId);

        String newName = request.getName().trim();
        if (!category.getName().equals(newName) && categoryRepository.existsByName(newName)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        category.update(newName, request.getDescription());
        return CategoryResponseDto.from(category);
    }

    @Transactional
    public CategoryResponseDto deleteCategory(Long userId, UUID categoryId) {
        Category category = getActiveCategory(categoryId);

        if (storeRepository.existsByCategory_CategoryIdAndIsActiveTrue(categoryId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        category.delete(userId);
        return CategoryResponseDto.from(category);
    }

    private Category getActiveCategory(UUID categoryId) {
        return categoryRepository.findByCategoryIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));
    }
}