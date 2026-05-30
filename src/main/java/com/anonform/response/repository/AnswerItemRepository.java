package com.anonform.response.repository;

import com.anonform.response.domain.AnswerItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnswerItemRepository extends JpaRepository<AnswerItem, UUID> {

    long countByQuestion_Id(UUID questionId);

    // ── RADIO — value 컬럼 기준 선택지별 카운트 ─────────────────

    @Query("""
            SELECT a.value, COUNT(a) FROM AnswerItem a
            WHERE a.question.id = :questionId AND a.value IS NOT NULL
            GROUP BY a.value ORDER BY COUNT(a) DESC
            """)
    List<Object[]> countByQuestionIdGroupByValue(@Param("questionId") UUID questionId);

    // ── CHECKBOX — values[] 배열 전체 로드 후 Java-side 집계 ─────

    @Query("SELECT a FROM AnswerItem a WHERE a.question.id = :questionId AND a.values IS NOT NULL")
    List<AnswerItem> findCheckboxAnswersByQuestionId(@Param("questionId") UUID questionId);

    // ── RATING / LINEAR_SCALE ────────────────────────────────────

    @Query("""
            SELECT AVG(a.numberValue) FROM AnswerItem a
            WHERE a.question.id = :questionId AND a.numberValue IS NOT NULL
            """)
    Double averageByQuestionId(@Param("questionId") UUID questionId);

    /** numberValue 값별 응답 수 분포 */
    @Query("""
            SELECT a.numberValue, COUNT(a) FROM AnswerItem a
            WHERE a.question.id = :questionId AND a.numberValue IS NOT NULL
            GROUP BY a.numberValue ORDER BY a.numberValue
            """)
    List<Object[]> distributionByQuestionId(@Param("questionId") UUID questionId);

    // ── SHORT_TEXT / LONG_TEXT ───────────────────────────────────

    /** 최신 순으로 최대 50개 텍스트 응답 조회 */
    @Query("""
            SELECT a.value FROM AnswerItem a
            WHERE a.question.id = :questionId AND a.value IS NOT NULL
            ORDER BY a.id DESC
            """)
    List<String> findTextAnswersByQuestionId(@Param("questionId") UUID questionId,
                                             Pageable pageable);
}
