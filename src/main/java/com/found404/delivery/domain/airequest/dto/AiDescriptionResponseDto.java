package com.found404.delivery.domain.airequest.dto;

import com.found404.delivery.domain.airequest.entity.AiRequest;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AiDescriptionResponseDto {

    private UUID aiRequestId;
    private String generatedDescription;

    public AiDescriptionResponseDto(UUID aiRequestId, String generatedDescription) {
        this.aiRequestId = aiRequestId;
        this.generatedDescription = generatedDescription;
    }

    public static AiDescriptionResponseDto from(AiRequest aiRequest) {
        return new AiDescriptionResponseDto(
                aiRequest.getId(),
                aiRequest.getResponseContent()
        );
    }
}
