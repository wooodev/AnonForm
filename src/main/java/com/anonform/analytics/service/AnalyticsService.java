package com.anonform.analytics.service;

import com.anonform.analytics.dto.AnalyticsResult;
import com.anonform.analytics.dto.AnalyticsResult.*;
import com.anonform.common.exception.ResourceNotFoundException;
import com.anonform.form.domain.Form;
import com.anonform.form.domain.Question;
import com.anonform.form.domain.QuestionType;
import com.anonform.form.repository.FormRepository;
import com.anonform.response.domain.AnswerItem;
import com.anonform.response.domain.FormResponse;
import com.anonform.response.repository.AnswerItemRepository;
import com.anonform.response.repository.FormResponseRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsService {

    private final FormRepository         formRepository;
    private final FormResponseRepository formResponseRepository;
    private final AnswerItemRepository   answerItemRepository;

    // ── 집계 ──────────────────────────────────────────────

    public FormAnalytics getAnalytics(UUID formId, UUID userId) {
        Form form = getOwnedForm(formId, userId);
        long total = formResponseRepository.countByForm_Id(formId);

        List<QuestionAnalytics> questions = form.getQuestions().stream()
                .map(q -> analyzeQuestion(q, formId))
                .toList();

        return new FormAnalytics(total, questions);
    }

    private QuestionAnalytics analyzeQuestion(Question q, UUID formId) {
        long answerCount = answerItemRepository.countByQuestion_Id(q.getId());
        Object data = switch (q.getType()) {
            case RADIO         -> choiceData(q.getId());
            case CHECKBOX      -> checkboxData(q.getId());
            case RATING,
                 LINEAR_SCALE  -> scaleData(q.getId());
            case SHORT_TEXT,
                 LONG_TEXT     -> textData(q.getId());
        };
        return new QuestionAnalytics(q.getId(), q.getTitle(), q.getType().name(),
                                     answerCount, data);
    }

    /** RADIO — value 컬럼 GROUP BY */
    private ChoiceData choiceData(UUID questionId) {
        List<OptionCount> options = answerItemRepository
                .countByQuestionIdGroupByValue(questionId)
                .stream()
                .map(row -> new OptionCount((String) row[0], (Long) row[1]))
                .toList();
        return new ChoiceData(options);
    }

    /** CHECKBOX — values[] 배열을 Java-side로 unnest 후 집계 */
    private ChoiceData checkboxData(UUID questionId) {
        Map<String, Long> counts = answerItemRepository
                .findCheckboxAnswersByQuestionId(questionId)
                .stream()
                .filter(a -> a.getValues() != null)
                .flatMap(a -> Arrays.stream(a.getValues()))
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        List<OptionCount> options = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new OptionCount(e.getKey(), e.getValue()))
                .toList();
        return new ChoiceData(options);
    }

    /** RATING / LINEAR_SCALE — 평균 + 값별 분포 */
    private ScaleData scaleData(UUID questionId) {
        Double avg = answerItemRepository.averageByQuestionId(questionId);
        List<NumberDistribution> dist = answerItemRepository
                .distributionByQuestionId(questionId)
                .stream()
                .map(row -> new NumberDistribution(
                        ((BigDecimal) row[0]).intValue(), (Long) row[1]))
                .toList();
        return new ScaleData(avg != null ? avg : 0.0, dist);
    }

    /** SHORT_TEXT / LONG_TEXT — 최신 50개 텍스트 목록 */
    private TextData textData(UUID questionId) {
        long total = answerItemRepository.countByQuestion_Id(questionId);
        List<String> texts = answerItemRepository.findTextAnswersByQuestionId(
                questionId, PageRequest.of(0, 50));
        return new TextData(texts, total);
    }

    // ── 개별 응답 ──────────────────────────────────────────

    public ResponsePage getResponses(UUID formId, UUID userId, int page, int size) {
        getOwnedForm(formId, userId);  // 소유자 검증
        Pageable pageable = PageRequest.of(page, size);
        Page<FormResponse> result = formResponseRepository
                .findByForm_IdOrderBySubmittedAtDesc(formId, pageable);

        List<ResponseSummary> content = result.getContent().stream()
                .map(r -> new ResponseSummary(r.getId(), r.getSubmittedAt(),
                                              r.getAnswers().size()))
                .toList();
        return new ResponsePage(content, result.getTotalElements(),
                                result.getTotalPages(), result.getNumber());
    }

    public ResponseDetail getResponseDetail(UUID formId, UUID userId, UUID responseId) {
        getOwnedForm(formId, userId);  // 소유자 검증
        FormResponse response = formResponseRepository
                .findByIdAndForm_Id(responseId, formId)
                .orElseThrow(() -> new ResourceNotFoundException("응답을 찾을 수 없습니다."));

        List<AnswerDetail> answers = response.getAnswers().stream()
                .map(a -> new AnswerDetail(
                        a.getQuestion().getId(),
                        a.getQuestion().getTitle(),
                        a.getQuestion().getType().name(),
                        a.getValue(),
                        a.getValues() != null ? Arrays.asList(a.getValues()) : null,
                        a.getNumberValue()))
                .toList();
        return new ResponseDetail(response.getId(), response.getSubmittedAt(), answers);
    }

    // ── CSV 내보내기 ────────────────────────────────────────

    public void writeCsv(UUID formId, UUID userId, PrintWriter out) {
        Form form = getOwnedForm(formId, userId);
        List<Question> questions = form.getQuestions();
        List<FormResponse> responses =
                formResponseRepository.findAllWithAnswersAndQuestions(formId);

        try (CSVWriter csv = new CSVWriter(out)) {
            csv.writeNext(buildHeader(questions));
            for (FormResponse r : responses) {
                csv.writeNext(buildRow(r, questions));
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 생성 중 오류 발생", e);
        }
    }

    // ── private helpers ───────────────────────────────────

    private Form getOwnedForm(UUID formId, UUID userId) {
        return formRepository.findByIdAndUser_Id(formId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("폼을 찾을 수 없습니다."));
    }

    private String[] buildHeader(List<Question> questions) {
        String[] header = new String[questions.size() + 1];
        header[0] = "제출일시";
        for (int i = 0; i < questions.size(); i++) {
            header[i + 1] = questions.get(i).getTitle();
        }
        return header;
    }

    private String[] buildRow(FormResponse response, List<Question> questions) {
        Map<UUID, AnswerItem> answerMap = response.getAnswers().stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a,
                                          (a, b) -> a));  // 중복 시 첫 번째 유지

        String[] row = new String[questions.size() + 1];
        row[0] = response.getSubmittedAt().toString();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            AnswerItem answer = answerMap.get(q.getId());
            row[i + 1] = formatAnswer(answer, q.getType());
        }
        return row;
    }

    private String formatAnswer(AnswerItem a, QuestionType type) {
        if (a == null) return "";
        return switch (type) {
            case SHORT_TEXT, LONG_TEXT, RADIO ->
                    a.getValue() != null ? a.getValue() : "";
            case CHECKBOX ->
                    a.getValues() != null ? String.join(", ", a.getValues()) : "";
            case RATING, LINEAR_SCALE ->
                    a.getNumberValue() != null ? a.getNumberValue().toPlainString() : "";
        };
    }
}
