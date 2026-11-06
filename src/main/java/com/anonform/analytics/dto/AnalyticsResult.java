package com.anonform.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Analytics 모듈의 모든 응답 DTO를 한 파일에 모음.
 * Controller/Service는 이 클래스의 중첩 레코드를 import해서 사용.
 */
public final class AnalyticsResult {

    private AnalyticsResult() {}

    // ── 집계 결과 ─────────────────────────────────────────

    /** GET /{id}/analytics 응답 */
    public record FormAnalytics(
            long totalResponses,
            List<QuestionAnalytics> questions
    ) {}

    public record QuestionAnalytics(
            UUID questionId,
            String questionTitle,
            String questionType,
            long answerCount,
            Object data   // ChoiceData | ScaleData | TextData
    ) {}

    /** RADIO / CHECKBOX */
    public record ChoiceData(List<OptionCount> options) {}
    public record OptionCount(String label, long count) {}

    /** RATING / LINEAR_SCALE */
    public record ScaleData(double average, List<NumberDistribution> distribution) {}
    public record NumberDistribution(int value, long count) {}

    /** SHORT_TEXT / LONG_TEXT */
    public record TextData(List<String> responses, long totalCount) {}

    // ── 개별 응답 ─────────────────────────────────────────

    /** GET /{id}/responses 페이지 응답 */
    public record ResponsePage(
            List<ResponseSummary> content,
            long totalElements,
            int totalPages,
            int currentPage
    ) {}

    public record ResponseSummary(
            UUID id,
            LocalDateTime submittedAt,
            int answerCount
    ) {}

    /** GET /{id}/responses/{respId} 상세 응답 */
    public record ResponseDetail(
            UUID id,
            LocalDateTime submittedAt,
            List<AnswerDetail> answers
    ) {}

    public record AnswerDetail(
            UUID questionId,
            String questionTitle,
            String questionType,
            String value,
            List<String> values,
            BigDecimal numberValue
    ) {}
}
