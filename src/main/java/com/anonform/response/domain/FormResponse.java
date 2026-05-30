package com.anonform.response.domain;

import com.anonform.form.domain.Form;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "form_responses")
@Getter
@NoArgsConstructor
public class FormResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(unique = true, nullable = false, length = 100)
    private String responseToken;

    private LocalDateTime submittedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerItem> answers = new ArrayList<>();

    // ── 팩토리 메서드 ─────────────────────────────────────

    public static FormResponse create(Form form, String responseToken) {
        FormResponse r   = new FormResponse();
        r.form           = form;
        r.responseToken  = responseToken;
        return r;
    }
}
