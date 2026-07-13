package com.found404.delivery.domain.airequest.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.base-url}") String baseUrl
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public GeminiResult generate(String finalPrompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", finalPrompt))
                ))
        );

        JsonNode response;
        try {
            response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            log.warn("Gemini 호출 실패", e);
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }

        if (response == null) {
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }

        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            log.warn("Gemini 응답에 candidates 없음: {}", response);
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }

        JsonNode candidate = candidates.get(0);
        String finishReason = candidate.path("finishReason").asText();
        if (!"STOP".equals(finishReason)) {
            log.warn("Gemini 비정상 종료: {}", finishReason);
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }

        JsonNode parts = candidate.path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            log.warn("Gemini 응답에 parts 없음: {}", candidate);
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }

        String text = parts.get(0).path("text").asText(null);
        if (text == null || text.isBlank()) {
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }

        String modelVersion = response.path("modelVersion").asText(model);

        return new GeminiResult(text.trim(), modelVersion);
    }
}