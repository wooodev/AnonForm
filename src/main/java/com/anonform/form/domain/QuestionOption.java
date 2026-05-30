package com.anonform.form.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "question_options")
@Getter
@NoArgsConstructor
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(nullable = false)
    private int position;

    // ── 팩토리 메서드 ─────────────────────────────────────

    public static QuestionOption create(Question question, String label, int position) {
        QuestionOption opt = new QuestionOption();
        opt.question = question;
        opt.label    = label;
        opt.position = position;
        return opt;
    }
}
