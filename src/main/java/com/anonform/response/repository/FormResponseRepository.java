package com.anonform.response.repository;

import com.anonform.response.domain.FormResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormResponseRepository extends JpaRepository<FormResponse, UUID> {

    boolean existsByForm_IdAndResponseToken(UUID formId, String responseToken);

    long countByForm_Id(UUID formId);

    /** 개별 응답 목록 — 최신 순 페이징 */
    Page<FormResponse> findByForm_IdOrderBySubmittedAtDesc(UUID formId, Pageable pageable);

    /** 소유자 검증용 단건 조회 */
    Optional<FormResponse> findByIdAndForm_Id(UUID id, UUID formId);

    /** CSV 내보내기 — 응답 + 답변 + 질문 한 번에 로드 (N+1 방지) */
    @Query("""
            SELECT DISTINCT fr FROM FormResponse fr
            LEFT JOIN FETCH fr.answers a
            LEFT JOIN FETCH a.question
            WHERE fr.form.id = :formId
            ORDER BY fr.submittedAt ASC
            """)
    List<FormResponse> findAllWithAnswersAndQuestions(@Param("formId") UUID formId);
}
