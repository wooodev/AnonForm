package com.anonform.analytics.controller;

import com.anonform.analytics.dto.AnalyticsResult;
import com.anonform.analytics.dto.AnalyticsResult.*;
import com.anonform.analytics.service.AnalyticsService;
import com.anonform.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/v1/forms/{id}/analytics
     * 질문 유형별 집계 결과 (차트 데이터)
     */
    @GetMapping("/{id}/analytics")
    public ApiResponse<FormAnalytics> getAnalytics(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ApiResponse.ok(analyticsService.getAnalytics(id, userId));
    }

    /**
     * GET /api/v1/forms/{id}/responses?page=0&size=20
     * 개별 응답 목록 — 최신 순 페이징
     */
    @GetMapping("/{id}/responses")
    public ApiResponse<ResponsePage> getResponses(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(analyticsService.getResponses(id, userId, page, size));
    }

    /**
     * GET /api/v1/forms/{id}/responses/{respId}
     * 개별 응답 상세 (질문별 답변 전체 포함)
     */
    @GetMapping("/{id}/responses/{respId}")
    public ApiResponse<ResponseDetail> getResponseDetail(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID respId) {
        return ApiResponse.ok(analyticsService.getResponseDetail(id, userId, respId));
    }

    /**
     * GET /api/v1/forms/{id}/export/csv
     * 응답 데이터 CSV 내보내기.
     * UTF-8 BOM(﻿) 포함 — Excel 한글 깨짐 방지.
     */
    @GetMapping("/{id}/export/csv")
    public void exportCsv(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"responses_" + id + ".csv\"");

        // UTF-8 BOM (U+FEFF) — PrintWriter가 UTF-8로 인코딩하므로 0xEF 0xBB 0xBF로 변환
        // Excel에서 한글이 깨지지 않도록 BOM을 먼저 씀
        response.getWriter().write('﻿');
        analyticsService.writeCsv(id, userId, response.getWriter());
    }
}
