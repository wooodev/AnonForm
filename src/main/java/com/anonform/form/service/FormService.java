package com.anonform.form.service;

import com.anonform.auth.domain.User;
import com.anonform.auth.repository.UserRepository;
import com.anonform.common.exception.FormClosedException;
import com.anonform.common.exception.ResourceNotFoundException;
import com.anonform.common.slug.SlugGenerator;
import com.anonform.form.domain.Form;
import com.anonform.form.domain.Question;
import com.anonform.form.domain.QuestionOption;
import com.anonform.form.dto.*;
import com.anonform.form.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class FormService {

    private final FormRepository  formRepository;
    private final UserRepository  userRepository;
    private final SlugGenerator   slugGenerator;

    // ── 조회 ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FormSummaryResponse> getMyForms(UUID userId) {
        return formRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FormSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FormDetailResponse getForm(UUID userId, UUID formId) {
        return FormDetailResponse.from(getOwnedForm(userId, formId));
    }

    /** 공개 폼 조회 — Phase 3 응답 제출 시에도 재사용 */
    @Transactional(readOnly = true)
    public FormDetailResponse getPublicForm(String slug) {
        Form form = formRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("폼을 찾을 수 없습니다."));
        if (!form.isActive()) {
            throw new FormClosedException("비활성화된 폼입니다.");
        }
        return FormDetailResponse.from(form);
    }

    /** Phase 3에서 응답 저장 전 검증용 — Form 엔티티 반환 */
    @Transactional(readOnly = true)
    public Form getOpenForm(String slug) {
        Form form = formRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("폼을 찾을 수 없습니다."));
        if (!form.isOpen()) {
            throw new FormClosedException("마감되었거나 비활성화된 폼입니다.");
        }
        return form;
    }

    // ── 생성 ──────────────────────────────────────────────

    public FormDetailResponse createForm(UUID userId, CreateFormRequest req) {
        User user = getUser(userId);
        Form form = Form.create(user, req.title(), req.description(),
                                slugGenerator.generateUnique(),
                                req.allowDuplicate(), req.maxResponses(), req.closesAt());
        attachQuestions(form, req.questions());
        return FormDetailResponse.from(formRepository.save(form));
    }

    // ── 수정 ──────────────────────────────────────────────

    public FormDetailResponse updateForm(UUID userId, UUID formId, UpdateFormRequest req) {
        Form form = getOwnedForm(userId, formId);
        form.updateDetails(req.title(), req.description(),
                           req.allowDuplicate(), req.maxResponses(), req.closesAt());
        if (req.questions() != null) {
            form.replaceQuestions(buildQuestions(form, req.questions()));
        }
        return FormDetailResponse.from(form);
    }

    public void updateFormStatus(UUID userId, UUID formId, UpdateFormStatusRequest req) {
        getOwnedForm(userId, formId).setActive(req.active());
    }

    // ── 삭제 ──────────────────────────────────────────────

    public void deleteForm(UUID userId, UUID formId) {
        Form form = getOwnedForm(userId, formId);
        formRepository.delete(form);
    }

    // ── 복제 ──────────────────────────────────────────────

    public FormDetailResponse duplicateForm(UUID userId, UUID formId) {
        Form original = getOwnedForm(userId, formId);
        User user     = getUser(userId);

        Form copy = Form.create(user, "[복사] " + original.getTitle(),
                                original.getDescription(),
                                slugGenerator.generateUnique(),
                                original.isAllowDuplicate(),
                                original.getMaxResponses(),
                                null);   // 마감일은 초기화

        List<Question> copiedQuestions = original.getQuestions().stream()
                .map(q -> {
                    Question newQ = Question.create(copy, q.getType(), q.getTitle(),
                                                   q.getDescription(), q.isRequired(),
                                                   q.getPosition(), q.getSettings());
                    q.getOptions().forEach(opt ->
                            newQ.getOptions().add(
                                    QuestionOption.create(newQ, opt.getLabel(), opt.getPosition())));
                    return newQ;
                }).toList();

        copy.replaceQuestions(copiedQuestions);
        return FormDetailResponse.from(formRepository.save(copy));
    }

    // ── private ──────────────────────────────────────────

    private Form getOwnedForm(UUID userId, UUID formId) {
        return formRepository.findByIdAndUser_Id(formId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("폼을 찾을 수 없습니다."));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private void attachQuestions(Form form, List<CreateQuestionRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        form.replaceQuestions(buildQuestions(form, reqs));
    }

    private List<Question> buildQuestions(Form form, List<CreateQuestionRequest> reqs) {
        List<Question> result = new ArrayList<>();
        for (int i = 0; i < reqs.size(); i++) {
            CreateQuestionRequest req = reqs.get(i);
            Question q = Question.create(form, req.type(), req.title(),
                                         req.description(), req.required(), i, req.settings());
            if (req.options() != null) {
                for (int j = 0; j < req.options().size(); j++) {
                    q.getOptions().add(
                            QuestionOption.create(q, req.options().get(j).label(), j));
                }
            }
            result.add(q);
        }
        return result;
    }
}
