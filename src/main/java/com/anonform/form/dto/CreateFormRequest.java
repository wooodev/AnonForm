package com.anonform.form.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateFormRequest(
        @NotBlank(message = "폼 제목은 필수입니다.")
        @Size(max = 300)
        String title,

        String description,

        /** 중복 응답 허용 여부 (기본: false) */
        Boolean allowDuplicate,

        /** 최대 응답 수 (null = 무제한) */
        Integer maxResponses,

        /** 응답 마감일 (null = 무제한) */
        LocalDateTime closesAt,

        @Valid
        List<CreateQuestionRequest> questions
) {}
