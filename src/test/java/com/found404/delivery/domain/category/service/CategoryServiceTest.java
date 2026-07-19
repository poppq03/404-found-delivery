package com.found404.delivery.domain.category.service;

import com.found404.delivery.domain.category.dto.CategoryResponseDto;
import com.found404.delivery.domain.category.entity.Category;
import com.found404.delivery.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("활성화된 카테고리 목록을 조회한다")
    void getCategories() {
        Category korean = category("한식");
        Category chinese = category("중식");
        Pageable pageable = PageRequest.of(0, 10);

        given(categoryRepository.findAllByIsActiveTrue(pageable))
                .willReturn(new PageImpl<>(List.of(korean, chinese), pageable, 2));

        Page<CategoryResponseDto> response = categoryService.searchCategories(null, pageable);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting(CategoryResponseDto::getName)
                .containsExactly("한식", "중식");
    }

    @Test
    @DisplayName("활성화된 카테고리가 하나도 없으면 빈 목록을 반환한다")
    void getCategoriesEmpty() {
        Pageable pageable = PageRequest.of(0, 10);

        given(categoryRepository.findAllByIsActiveTrue(pageable))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<CategoryResponseDto> response = categoryService.searchCategories(null, pageable);

        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("카테고리를 DTO로 변환하면 id와 name이 매핑된다")
    void categoryMappedToDto() {
        UUID categoryId = UUID.randomUUID();
        Category category = category("치킨");
        ReflectionTestUtils.setField(category, "categoryId", categoryId);
        Pageable pageable = PageRequest.of(0, 10);

        given(categoryRepository.findAllByIsActiveTrue(pageable))
                .willReturn(new PageImpl<>(List.of(category), pageable, 1));

        CategoryResponseDto response = categoryService.searchCategories(null, pageable).getContent().get(0);

        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getName()).isEqualTo("치킨");
    }

    private Category category(String name) {
        Category category = Category.create(name, null);
        ReflectionTestUtils.setField(category, "categoryId", UUID.randomUUID());
        return category;
    }
}
