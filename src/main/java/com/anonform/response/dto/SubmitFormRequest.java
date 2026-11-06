package com.anonform.response.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitFormRequest(
        @NotNull(message = "answers는 필수입니다.")
        @NotEmpty(message = "최소 1개의 답변이 필요합니다.")
        List<@Valid AnswerItemRequest> answers
) {}
