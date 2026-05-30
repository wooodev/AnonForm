package com.anonform.form.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** PUT /api/v1/forms/{id} — 폼 전체 교체 */
public record UpdateFormRequest(
        @NotBlank(message = "폼 제목은 필수입니다.")
        @Size(max = 300)
        String title,

        String description,

        Boolean allowDuplicate,

        /** null 전송 = 무제한으로 변경 */
        Integer maxResponses,

        /** null 전송 = 마감일 제거 */
        LocalDateTime closesAt,

        @Valid
        List<CreateQuestionRequest> questions
) {}
