package com.anonform.form.dto;

import com.anonform.form.domain.Question;

import java.util.List;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        String type,
        String title,
        String description,
        boolean required,
        int position,
        String settings,
        List<QuestionOptionResponse> options
) {
    public static QuestionResponse from(Question q) {
        return new QuestionResponse(
                q.getId(),
                q.getType().name(),
                q.getTitle(),
                q.getDescription(),
                q.isRequired(),
                q.getPosition(),
                q.getSettings(),
                q.getOptions().stream().map(QuestionOptionResponse::from).toList()
        );
    }
}
