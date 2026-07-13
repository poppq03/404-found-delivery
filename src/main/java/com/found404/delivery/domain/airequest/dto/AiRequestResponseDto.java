package com.found404.delivery.domain.airequest.dto;

import com.found404.delivery.domain.airequest.entity.AiRequest;
import com.found404.delivery.domain.airequest.entity.AiRequestStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AiRequestResponseDto {

    private UUID aiRequestId;
    private UUID menuId;
    private String prompt;
    private String responseContent;
    private String aiModel;
    private AiRequestStatus status;
    private LocalDateTime createdAt;

    public AiRequestResponseDto(UUID aiRequestId, UUID menuId, String prompt, String responseContent,
                                String aiModel, AiRequestStatus status, LocalDateTime createdAt) {
        this.aiRequestId = aiRequestId;
        this.menuId = menuId;
        this.prompt = prompt;
        this.responseContent = responseContent;
        this.aiModel = aiModel;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static AiRequestResponseDto from(AiRequest aiRequest) {
        return new AiRequestResponseDto(
                aiRequest.getId(),
                aiRequest.getMenuId(),
                aiRequest.getPrompt(),
                aiRequest.getResponseContent(),
                aiRequest.getAiModel(),
                aiRequest.getStatus(),
                aiRequest.getCreatedAt()
        );
    }
}