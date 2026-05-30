package com.anonform.form.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_required")
    private boolean required = false;

    @Column(nullable = false)
    private int position;

    // 유형별 추가 설정 (척도 범위, 별점 최대값 등) - JSON 저장
    @Column(columnDefinition = "jsonb")
    private String settings;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestionOption> options = new ArrayList<>();

    // ── 팩토리 메서드 ─────────────────────────────────────

    public static Question create(Form form, QuestionType type, String title,
                                  String description, Boolean required,
                                  int position, String settings) {
        Question q  = new Question();
        q.form      = form;
        q.type      = type;
        q.title     = title;
        q.description = description;
        q.required  = Boolean.TRUE.equals(required);
        q.position  = position;
        q.settings  = settings;
        return q;
    }
}
