package com.anonform.form.dto;

import com.anonform.form.domain.Form;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FormDetailResponse(
        UUID id,
        String title,
        String description,
        String slug,
        boolean active,
        boolean allowDuplicate,
        Integer maxResponses,
        LocalDateTime closesAt,
        List<QuestionResponse> questions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FormDetailResponse from(Form form) {
        return new FormDetailResponse(
                form.getId(),
                form.getTitle(),
                form.getDescription(),
                form.getSlug(),
                form.isActive(),
                form.isAllowDuplicate(),
                form.getMaxResponses(),
                form.getClosesAt(),
                form.getQuestions().stream().map(QuestionResponse::from).toList(),
                form.getCreatedAt(),
                form.getUpdatedAt()
        );
    }
}
