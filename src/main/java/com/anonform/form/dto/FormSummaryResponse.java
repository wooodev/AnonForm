package com.anonform.form.dto;

import com.anonform.form.domain.Form;

import java.time.LocalDateTime;
import java.util.UUID;

public record FormSummaryResponse(
        UUID id,
        String title,
        String slug,
        boolean active,
        Integer maxResponses,
        LocalDateTime closesAt,
        int questionCount,
        LocalDateTime createdAt
) {
    public static FormSummaryResponse from(Form form) {
        return new FormSummaryResponse(
                form.getId(),
                form.getTitle(),
                form.getSlug(),
                form.isActive(),
                form.getMaxResponses(),
                form.getClosesAt(),
                form.getQuestions().size(),
                form.getCreatedAt()
        );
    }
}
