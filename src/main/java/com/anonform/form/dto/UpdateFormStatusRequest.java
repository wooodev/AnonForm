package com.anonform.form.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateFormStatusRequest(
        @NotNull(message = "active 값은 필수입니다.")
        Boolean active
) {}
