package com.found404.delivery.domain.airequest.service;

import com.found404.delivery.domain.airequest.client.GeminiClient;
import com.found404.delivery.domain.airequest.client.GeminiResult;
import com.found404.delivery.domain.airequest.dto.AiDescriptionRequestDto;
import com.found404.delivery.domain.airequest.dto.AiDescriptionResponseDto;
import com.found404.delivery.domain.airequest.entity.AiRequest;
import com.found404.delivery.domain.airequest.entity.AiRequestStatus;
import com.found404.delivery.domain.airequest.repository.AiRequestRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiRequestService {

    private static final String PROMPT_SUFFIX = " 답변을 최대한 간결하게 50자 이하로";

    private final GeminiClient geminiClient;
    private final AiRequestRepository aiRequestRepository;

    public AiDescriptionResponseDto generateDescription(
            AiDescriptionRequestDto request, Long userId, String role) {

        // [TEMP] TODO: UserRole enum 연동 시 추가 예정
        if (!"OWNER".equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String finalPrompt = request.getPrompt() + PROMPT_SUFFIX;

        GeminiResult result;
        try {
            result = geminiClient.generate(finalPrompt);
        } catch (CustomException e) {
            // 설명 생성 실패해도 로그 남김
            saveLog(request, userId, finalPrompt, null, null, AiRequestStatus.FAILED);
            throw e;
        }

        AiRequest saved = saveLog(request, userId, finalPrompt,
                result.text(), result.modelVersion(), AiRequestStatus.SUCCESS);

        return AiDescriptionResponseDto.from(saved);
    }


    private AiRequest saveLog(AiDescriptionRequestDto request, Long userId, String finalPrompt,
                              String responseContent, String aiModel, AiRequestStatus status) {
        AiRequest aiRequest = AiRequest.builder()
                .userId(userId)
                .storeId(request.getStoreId())
                .menuId(request.getMenuId())
                .prompt(request.getPrompt())
                .finalPrompt(finalPrompt)
                .responseContent(responseContent)
                .aiModel(aiModel)
                .status(status)
                .build();
        return aiRequestRepository.save(aiRequest);
    }
}
