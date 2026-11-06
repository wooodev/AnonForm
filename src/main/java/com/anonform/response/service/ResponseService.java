package com.anonform.response.service;

import com.anonform.common.exception.ResourceNotFoundException;
import com.anonform.form.domain.Form;
import com.anonform.form.domain.Question;
import com.anonform.response.domain.AnswerItem;
import com.anonform.response.domain.FormResponse;
import com.anonform.response.dto.AnswerItemRequest;
import com.anonform.response.repository.FormResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ResponseService {

    private final FormResponseRepository formResponseRepository;

    /**
     * 폼 응답 저장.
     * form.getQuestions()로 질문 맵을 구성해 N+1 없이 처리.
     */
    public void saveResponse(Form form, List<AnswerItemRequest> answers, String responseToken) {
        Map<UUID, Question> questionMap = form.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        FormResponse response = FormResponse.create(form, responseToken);

        for (AnswerItemRequest req : answers) {
            Question question = questionMap.get(req.questionId());
            if (question == null) {
                throw new ResourceNotFoundException(
                        "질문을 찾을 수 없습니다: " + req.questionId());
            }
            response.getAnswers().add(
                    AnswerItem.create(
                            response,
                            question,
                            req.value(),
                            toStringArray(req.values()),
                            req.numberValue()
                    )
            );
        }

        formResponseRepository.save(response);
    }

    @Transactional(readOnly = true)
    public long countResponses(UUID formId) {
        return formResponseRepository.countByForm_Id(formId);
    }

    // ── private ──────────────────────────────────────────

    private String[] toStringArray(List<String> list) {
        return (list == null) ? null : list.toArray(String[]::new);
    }
}
