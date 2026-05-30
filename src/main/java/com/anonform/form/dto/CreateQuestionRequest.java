package com.anonform.form.dto;

import com.anonform.form.domain.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateQuestionRequest(
        @NotNull(message = "질문 유형은 필수입니다.")
        QuestionType type,

        @NotBlank(message = "질문 내용은 필수입니다.")
        @Size(max = 500)
        String title,

        String description,

        Boolean required,

        /** 유형별 추가 설정 — JSON 문자열
         *  예) rating: {"max":5}  linear_scale: {"min":1,"max":10,"minLabel":"매우 나쁨","maxLabel":"매우 좋음"} */
        String settings,

        /** radio / checkbox 유형에서만 사용 */
        @Valid
        List<CreateQuestionOptionRequest> options
) {}
