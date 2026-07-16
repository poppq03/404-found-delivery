package com.found404.delivery.domain.airequest.client;

/**
 * Gemini 호출 결과를 담는 DTO
 * - text: 생성된 메뉴 설명 텍스트
 * - modelVersion: 실제 사용된 모델명
 */
public record GeminiResult(String text, String modelVersion) {
}