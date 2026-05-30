package com.anonform.form.dto;

import com.anonform.form.domain.QuestionOption;

import java.util.UUID;

public record QuestionOptionResponse(
        UUID id,
        String label,
        int position
) {
    public static QuestionOptionResponse from(QuestionOption opt) {
        return new QuestionOptionResponse(opt.getId(), opt.getLabel(), opt.getPosition());
    }
}
