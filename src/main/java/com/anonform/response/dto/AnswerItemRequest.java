package com.anonform.response.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AnswerItemRequest(
        @NotNull(message = "questionId는 필수입니다.")
        UUID questionId,

        /** SHORT_TEXT, LONG_TEXT */
        String value,

        /** CHECKBOX — 다중 선택 */
        List<String> values,

        /** RATING, LINEAR_SCALE */
        BigDecimal numberValue
) {}
