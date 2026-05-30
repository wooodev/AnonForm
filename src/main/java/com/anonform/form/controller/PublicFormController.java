package com.anonform.form.controller;

import com.anonform.common.response.ApiResponse;
import com.anonform.form.dto.FormDetailResponse;
import com.anonform.form.service.FormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/forms")
@RequiredArgsConstructor
public class PublicFormController {

    private final FormService formService;

    /**
     * GET /api/v1/public/forms/{slug}
     * 인증 없이 접근 가능. 활성화 상태인 폼만 반환.
     */
    @GetMapping("/{slug}")
    public ApiResponse<FormDetailResponse> getPublicForm(@PathVariable String slug) {
        return ApiResponse.ok(formService.getPublicForm(slug));
    }
}
