package com.found404.delivery.domain.airequest.dto;

import com.found404.delivery.domain.airequest.entity.AiRequest;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class AiRequestListResponseDto {

    private List<AiRequestResponseDto> content;
    private int page;
    private int size;
    private Long totalElements;
    private int totalPages;

    public AiRequestListResponseDto(List<AiRequestResponseDto> content, int page, int size,
                                    Long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static AiRequestListResponseDto from(Page<AiRequest> result) {
        List<AiRequestResponseDto> content = result.getContent().stream()
                .map(AiRequestResponseDto::from)
                .toList();

        return new AiRequestListResponseDto(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}