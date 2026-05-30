package com.anonform.form.controller;

import com.anonform.common.response.ApiResponse;
import com.anonform.form.dto.*;
import com.anonform.form.service.FormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    /** GET /api/v1/forms — 내 폼 목록 */
    @GetMapping
    public ApiResponse<List<FormSummaryResponse>> getMyForms(
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.ok(formService.getMyForms(userId));
    }

    /** POST /api/v1/forms — 폼 생성 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FormDetailResponse> createForm(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateFormRequest request) {
        return ApiResponse.ok(formService.createForm(userId, request));
    }

    /** GET /api/v1/forms/{id} — 폼 상세 (질문 포함) */
    @GetMapping("/{id}")
    public ApiResponse<FormDetailResponse> getForm(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ApiResponse.ok(formService.getForm(userId, id));
    }

    /** PUT /api/v1/forms/{id} — 폼 전체 수정 */
    @PutMapping("/{id}")
    public ApiResponse<FormDetailResponse> updateForm(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFormRequest request) {
        return ApiResponse.ok(formService.updateForm(userId, id, request));
    }

    /** DELETE /api/v1/forms/{id} — 폼 삭제 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForm(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        formService.deleteForm(userId, id);
    }

    /** PATCH /api/v1/forms/{id}/status — 활성화/비활성화 */
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFormStatusRequest request) {
        formService.updateFormStatus(userId, id, request);
        return ApiResponse.ok();
    }

    /** POST /api/v1/forms/{id}/duplicate — 폼 복제 */
    @PostMapping("/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FormDetailResponse> duplicateForm(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ApiResponse.ok(formService.duplicateForm(userId, id));
    }
}
