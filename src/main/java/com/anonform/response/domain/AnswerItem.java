package com.anonform.response.domain;

import com.anonform.form.domain.Question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "answer_items")
@Getter
@NoArgsConstructor
public class AnswerItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private FormResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String value;

    // 다중 선택(checkbox) 답변 - PostgreSQL TEXT[] 타입
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "values", columnDefinition = "TEXT[]")
    private String[] values;

    // 별점/선형 척도 숫자 값
    private BigDecimal numberValue;

    // ── 팩토리 메서드 ─────────────────────────────────────

    public static AnswerItem create(FormResponse response, Question question,
                                    String value, String[] values,
                                    BigDecimal numberValue) {
        AnswerItem item  = new AnswerItem();
        item.response    = response;
        item.question    = question;
        item.value       = value;
        item.values      = values;
        item.numberValue = numberValue;
        return item;
    }
}
