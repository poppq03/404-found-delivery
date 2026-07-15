package com.found404.delivery.domain.airequest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "p_ai_request",
        indexes = @Index(name = "idx_p_ai_request_menu_id", columnList = "menu_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AiRequest {

    @Id
    @UuidGenerator
    @Column(name = "ai_request_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "menu_id")
    private UUID menuId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "final_prompt", columnDefinition = "TEXT")
    private String finalPrompt;

    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;

    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiRequestStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Builder
    private AiRequest(UUID menuId, UUID storeId, Long userId, String prompt,
                      String finalPrompt, String responseContent, String aiModel,
                      AiRequestStatus status) {
        this.menuId = menuId;
        this.storeId = storeId;
        this.userId = userId;
        this.prompt = prompt;
        this.finalPrompt = finalPrompt;
        this.responseContent = responseContent;
        this.aiModel = aiModel;
        this.status = status;
    }
}