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

        given(categoryRepository.findAllByIsActiveTrue())
                .willReturn(List.of(korean, chinese));

        List<CategoryResponseDto> response = categoryService.getCategories();

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting(CategoryResponseDto::getName)
                .containsExactly("한식", "중식");
    }

    @Test
    @DisplayName("활성화된 카테고리가 하나도 없으면 빈 목록을 반환한다")
    void getCategoriesEmpty() {
        given(categoryRepository.findAllByIsActiveTrue())
                .willReturn(List.of());

        List<CategoryResponseDto> response = categoryService.getCategories();

        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("카테고리를 DTO로 변환하면 id와 name이 매핑된다")
    void categoryMappedToDto() {
        UUID categoryId = UUID.randomUUID();
        Category category = category("치킨");
        ReflectionTestUtils.setField(category, "categoryId", categoryId);

        given(categoryRepository.findAllByIsActiveTrue())
                .willReturn(List.of(category));

        CategoryResponseDto response = categoryService.getCategories().get(0);

        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getName()).isEqualTo("치킨");
    }

    private Category category(String name) {
        Category category = new Category();
        ReflectionTestUtils.setField(category, "categoryId", UUID.randomUUID());
        ReflectionTestUtils.setField(category, "name", name);
        ReflectionTestUtils.setField(category, "isActive", true);
        return category;
    }
}