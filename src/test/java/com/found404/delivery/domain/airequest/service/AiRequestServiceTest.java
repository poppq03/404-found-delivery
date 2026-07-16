package com.found404.delivery.domain.airequest.service;

import com.found404.delivery.domain.airequest.client.GeminiClient;
import com.found404.delivery.domain.airequest.client.GeminiResult;
import com.found404.delivery.domain.airequest.dto.AiDescriptionRequestDto;
import com.found404.delivery.domain.airequest.dto.AiDescriptionResponseDto;
import com.found404.delivery.domain.airequest.dto.AiRequestListResponseDto;
import com.found404.delivery.domain.airequest.entity.AiRequest;
import com.found404.delivery.domain.airequest.entity.AiRequestStatus;
import com.found404.delivery.domain.airequest.repository.AiRequestRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.assertj.core.api.ThrowableAssert;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AiRequestServiceTest {

    @Mock
    private GeminiClient geminiClient;
    @Mock
    private AiRequestRepository aiRequestRepository;

    @InjectMocks
    private AiRequestService aiRequestService;

    private static final String PROMPT_SUFFIX = " 답변을 최대한 간결하게 50자 이하로";

    private final Long userId = 1L;
    private final UUID storeId = UUID.randomUUID();
    private final UUID menuId = UUID.randomUUID();
    private final UUID aiRequestId = UUID.randomUUID();

    private AiDescriptionRequestDto descRequest(String prompt) {
        AiDescriptionRequestDto dto = new AiDescriptionRequestDto();
        dto.setPrompt(prompt);
        dto.setStoreId(storeId);
        dto.setMenuId(menuId);
        return dto;
    }

    private void assertErrorCode(ThrowableAssert.ThrowingCallable callable, ErrorCode expected) {
        assertThatThrownBy(callable)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("generateDescription")
    class GenerateDescription {

        @Test
        @DisplayName("성공 - 프롬프트에 접미사 결합, status=SUCCESS로 로그 저장, 생성 설명 반환")
        void success() {
            AiDescriptionRequestDto request = descRequest("매콤한 떡볶이 설명 만들어줘");
            GeminiResult geminiResult = new GeminiResult("불맛 가득 매콤 떡볶이", "gemini-1.5-flash");
            when(geminiClient.generate(anyString())).thenReturn(geminiResult);
            when(aiRequestRepository.save(any(AiRequest.class))).thenAnswer(inv -> {
                AiRequest a = inv.getArgument(0);
                ReflectionTestUtils.setField(a, "id", aiRequestId);
                return a;
            });

            AiDescriptionResponseDto res =
                    aiRequestService.generateDescription(request, userId, "OWNER");

            // 응답
            assertThat(res.getAiRequestId()).isEqualTo(aiRequestId);
            assertThat(res.getGeneratedDescription()).isEqualTo("불맛 가득 매콤 떡볶이");

            // Gemini에 넘어간 최종 프롬프트: 원본 프롬프트로 시작하고 + 접미사가 덧붙었는지
            // (접미사 문자열을 테스트에서 하드코딩 복제하지 않고 구조로 검증 → 서비스 문구가 바뀌어도 안 깨짐)
            ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
            verify(geminiClient).generate(promptCaptor.capture());
            assertThat(promptCaptor.getValue())
                    .startsWith("매콤한 떡볶이 설명 만들어줘")
                    .isNotEqualTo("매콤한 떡볶이 설명 만들어줘"); // 접미사가 실제로 붙었다

            // 저장된 로그 검증
            ArgumentCaptor<AiRequest> savedCaptor = ArgumentCaptor.forClass(AiRequest.class);
            verify(aiRequestRepository).save(savedCaptor.capture());
            AiRequest saved = savedCaptor.getValue();
            assertThat(saved.getStatus()).isEqualTo(AiRequestStatus.SUCCESS);
            assertThat(saved.getResponseContent()).isEqualTo("불맛 가득 매콤 떡볶이");
            assertThat(saved.getAiModel()).isEqualTo("gemini-1.5-flash");
            // 로그에 저장된 finalPrompt는 Gemini에 넘어간 값과 동일해야 함
            assertThat(saved.getFinalPrompt()).isEqualTo(promptCaptor.getValue());
            assertThat(saved.getPrompt()).isEqualTo("매콤한 떡볶이 설명 만들어줘");
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getStoreId()).isEqualTo(storeId);
            assertThat(saved.getMenuId()).isEqualTo(menuId);
        }

        @Test
        @DisplayName("Gemini 실패(CustomException) - status=FAILED로 로그 저장 후 예외 전파")
        void geminiFail_savesFailedLog_andRethrows() {
            AiDescriptionRequestDto request = descRequest("실패 유도 프롬프트");
            when(geminiClient.generate(anyString()))
                    .thenThrow(new CustomException(ErrorCode.AI_GENERATION_FAILED));

            assertErrorCode(
                    () -> aiRequestService.generateDescription(request, userId, "OWNER"),
                    ErrorCode.AI_GENERATION_FAILED);

            // 실패해도 FAILED 로그는 남김
            ArgumentCaptor<AiRequest> savedCaptor = ArgumentCaptor.forClass(AiRequest.class);
            verify(aiRequestRepository).save(savedCaptor.capture());
            AiRequest saved = savedCaptor.getValue();
            assertThat(saved.getStatus()).isEqualTo(AiRequestStatus.FAILED);
            assertThat(saved.getResponseContent()).isNull();
            assertThat(saved.getAiModel()).isNull();
            assertThat(saved.getFinalPrompt()).startsWith("실패 유도 프롬프트");
        }

        @Test
        @DisplayName("OWNER가 아니면 FORBIDDEN - Gemini 호출/로그 저장 모두 없음")
        void forbidden_whenNotOwner() {
            AiDescriptionRequestDto request = descRequest("아무 프롬프트");

            assertErrorCode(
                    () -> aiRequestService.generateDescription(request, userId, "MANAGER"),
                    ErrorCode.FORBIDDEN);

            verify(geminiClient, never()).generate(anyString());
            verify(aiRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getRequests")
    class GetRequests {

        private AiRequest aiRequestFixture() {
            AiRequest a = AiRequest.builder()
                    .userId(userId)
                    .storeId(storeId)
                    .menuId(menuId)
                    .prompt("떡볶이 설명")
                    .finalPrompt("떡볶이 설명" + PROMPT_SUFFIX)
                    .responseContent("매콤한 떡볶이")
                    .aiModel("gemini-1.5-flash")
                    .status(AiRequestStatus.SUCCESS)
                    .build();
            ReflectionTestUtils.setField(a, "id", aiRequestId);
            return a;
        }

        private Page<AiRequest> pageOf(AiRequest... items) {
            return new PageImpl<>(List.of(items), PageRequest.of(0, 10), items.length);
        }

        @Test
        @DisplayName("정상 조회 - content 매핑")
        void success_mapsContent() {
            when(aiRequestRepository.search(any(), any(), any(), any()))
                    .thenReturn(pageOf(aiRequestFixture()));

            AiRequestListResponseDto res = aiRequestService.getRequests(
                    userId, "MANAGER", menuId, null, PageRequest.of(0, 10));

            assertThat(res.getContent()).hasSize(1);
            assertThat(res.getContent().get(0).getAiRequestId()).isEqualTo(aiRequestId);
            assertThat(res.getContent().get(0).getStatus()).isEqualTo(AiRequestStatus.SUCCESS);
        }

        @Test
        @DisplayName("OWNER는 본인 이력만 - ownerScope=userId 로 검색")
        void ownerScope_isUserId_forOwner() {
            when(aiRequestRepository.search(any(), any(), any(), any())).thenReturn(pageOf());
            ArgumentCaptor<Long> ownerScope = ArgumentCaptor.forClass(Long.class);

            aiRequestService.getRequests(userId, "OWNER", null, null, PageRequest.of(0, 10));

            verify(aiRequestRepository).search(ownerScope.capture(), any(), any(), any());
            assertThat(ownerScope.getValue()).isEqualTo(userId);
        }

        @Test
        @DisplayName("MANAGER는 전체 이력 - ownerScope=null 로 검색")
        void ownerScope_isNull_forManager() {
            when(aiRequestRepository.search(any(), any(), any(), any())).thenReturn(pageOf());
            ArgumentCaptor<Long> ownerScope = ArgumentCaptor.forClass(Long.class);

            aiRequestService.getRequests(userId, "MANAGER", null, null, PageRequest.of(0, 10));

            verify(aiRequestRepository).search(ownerScope.capture(), any(), any(), any());
            assertThat(ownerScope.getValue()).isNull();
        }

        @Test
        @DisplayName("허용되지 않은 page size는 10으로 강제")
        void invalidSize_forcedTo10() {
            when(aiRequestRepository.search(any(), any(), any(), any())).thenReturn(pageOf());
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

            aiRequestService.getRequests(userId, "MANAGER", null, null, PageRequest.of(0, 20));

            verify(aiRequestRepository).search(any(), any(), any(), captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("정렬 미허용 필드는 무시되고 createdAt desc 기본 적용")
        void invalidSort_defaultsToCreatedAtDesc() {
            when(aiRequestRepository.search(any(), any(), any(), any())).thenReturn(pageOf());
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            Pageable withBadSort = PageRequest.of(0, 10, Sort.by("evilField").ascending());

            aiRequestService.getRequests(userId, "MANAGER", null, null, withBadSort);

            verify(aiRequestRepository).search(any(), any(), any(), captor.capture());
            Sort.Order createdAt = captor.getValue().getSort().getOrderFor("createdAt");
            assertThat(createdAt).isNotNull();
            assertThat(createdAt.getDirection()).isEqualTo(Sort.Direction.DESC);
            assertThat(captor.getValue().getSort().getOrderFor("evilField")).isNull();
        }

        @Test
        @DisplayName("status 필터가 그대로 repository로 전달됨")
        void statusFilter_passedThrough() {
            when(aiRequestRepository.search(any(), any(), any(), any())).thenReturn(pageOf());
            ArgumentCaptor<AiRequestStatus> statusCaptor = ArgumentCaptor.forClass(AiRequestStatus.class);

            aiRequestService.getRequests(userId, "MASTER", menuId, AiRequestStatus.FAILED, PageRequest.of(0, 10));

            verify(aiRequestRepository).search(any(), eq(menuId), statusCaptor.capture(), any());
            assertThat(statusCaptor.getValue()).isEqualTo(AiRequestStatus.FAILED);
        }

        @Test
        @DisplayName("권한 없음(CUSTOMER)이면 FORBIDDEN - 검색 미호출")
        void forbidden_forCustomer() {
            assertErrorCode(
                    () -> aiRequestService.getRequests(userId, "CUSTOMER", null, null, PageRequest.of(0, 10)),
                    ErrorCode.FORBIDDEN);

            verify(aiRequestRepository, never()).search(any(), any(), any(), any());
        }
    }
}
