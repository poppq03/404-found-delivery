package com.found404.delivery.domain.airequest.service;

import com.found404.delivery.domain.airequest.client.GeminiClient;
import com.found404.delivery.domain.airequest.client.GeminiResult;
import com.found404.delivery.domain.airequest.dto.AiDescriptionRequestDto;
import com.found404.delivery.domain.airequest.dto.AiDescriptionResponseDto;
import com.found404.delivery.domain.airequest.dto.AiRequestListResponseDto;
import com.found404.delivery.domain.airequest.entity.AiRequest;
import com.found404.delivery.domain.airequest.entity.AiRequestStatus;
import com.found404.delivery.domain.airequest.repository.AiRequestRepository;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiRequestService {

    private static final String PROMPT_SUFFIX = " 답변을 최대한 간결하게 50자 이하로";
    private static final Set<String> ALLOWED_SORT = Set.of("createdAt");

    private final GeminiClient geminiClient;
    private final AiRequestRepository aiRequestRepository;

    public AiDescriptionResponseDto generateDescription(
            AiDescriptionRequestDto request, Long userId, String role) {

        validateOwnerRole(role);

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

    @Transactional(readOnly = true)
    public AiRequestListResponseDto getRequests(
            Long userId, String role, UUID menuId, AiRequestStatus status, Pageable pageable) {

        validateHistoryAccess(role);

        // OWNER는 본인 요청 이력만, MANAGER/MASTER는 전체
        Long ownerScope = (Role.valueOf(role) == Role.OWNER) ? userId : null;

        List<Sort.Order> orders = pageable.getSort().stream()
                .filter(order -> ALLOWED_SORT.contains(order.getProperty()))
                .collect(Collectors.toList());
        if (orders.isEmpty()) {
            orders.add(Sort.Order.desc("createdAt"));
        }

        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }

        Pageable fixedPageable = PageRequest.of(pageable.getPageNumber(), size, Sort.by(orders));

        Page<AiRequest> result = aiRequestRepository.search(ownerScope, menuId, status, fixedPageable);
        return AiRequestListResponseDto.from(result);
    }

    // ===== private 헬퍼 =====

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

    // AI 설명 생성 권한 (OWNER)
    private void validateOwnerRole(String role) {
        if (Role.valueOf(role) != Role.OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    // AI 이력 조회 권한 (OWNER / MANAGER / MASTER)
    private void validateHistoryAccess(String role) {
        Role r = Role.valueOf(role);
        if (r != Role.OWNER && r != Role.MANAGER && r != Role.MASTER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
