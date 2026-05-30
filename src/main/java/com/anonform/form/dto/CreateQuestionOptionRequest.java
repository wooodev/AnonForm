package com.anonform.form.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQuestionOptionRequest(
        @NotBlank(message = "선택지 내용은 필수입니다.")
        @Size(max = 200)
        String label
) {}
