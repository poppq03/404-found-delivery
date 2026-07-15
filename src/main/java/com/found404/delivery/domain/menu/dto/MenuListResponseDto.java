package com.found404.delivery.domain.menu.dto;

import com.found404.delivery.domain.menu.entity.Menu;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
public class MenuListResponseDto {

    private List<MenuSummaryResponseDto> content;
    private int page;
    private int size;
    private Long totalElements;
    private int totalPages;

    public MenuListResponseDto(List<MenuSummaryResponseDto> content, int page, int size, Long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static MenuListResponseDto from(Page<Menu> menuPage, Function<Menu, String> imageUrlResolver) {
        List<MenuSummaryResponseDto> content = menuPage.getContent().stream()
                .map(menu -> MenuSummaryResponseDto.from(menu, imageUrlResolver.apply(menu)))
                .toList();

        return new MenuListResponseDto(
                content,
                menuPage.getNumber(),
                menuPage.getSize(),
                menuPage.getTotalElements(),
                menuPage.getTotalPages()
        );
    }
}