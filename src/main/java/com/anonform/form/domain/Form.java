package com.anonform.form.domain;

import com.anonform.auth.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "forms")
@Getter
@NoArgsConstructor
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "slug", unique = true, nullable = false, length = 50)
    private String slug;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "allow_duplicate")
    private boolean allowDuplicate = false;

    private Integer maxResponses;

    private LocalDateTime closesAt;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Question> questions = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── 팩토리 메서드 ─────────────────────────────────────

    public static Form create(User user, String title, String description, String slug,
                              Boolean allowDuplicate, Integer maxResponses,
                              LocalDateTime closesAt) {
        Form f = new Form();
        f.user           = user;
        f.title          = title;
        f.description    = description;
        f.slug           = slug;
        f.allowDuplicate = Boolean.TRUE.equals(allowDuplicate);
        f.maxResponses   = maxResponses;
        f.closesAt       = closesAt;
        return f;
    }

    // ── 변경 메서드 (JPA 관리 엔티티 상태 변경) ──────────────

    public void updateDetails(String title, String description, Boolean allowDuplicate,
                              Integer maxResponses, LocalDateTime closesAt) {
        this.title          = title;
        this.description    = description;
        this.allowDuplicate = Boolean.TRUE.equals(allowDuplicate);
        this.maxResponses   = maxResponses;
        this.closesAt       = closesAt;
        this.updatedAt      = LocalDateTime.now();
    }

    public void setActive(boolean active) {
        this.active    = active;
        this.updatedAt = LocalDateTime.now();
    }

    /** 질문 목록을 완전 교체. orphanRemoval 이 삭제를 담당. */
    public void replaceQuestions(List<Question> incoming) {
        this.questions.clear();
        this.questions.addAll(incoming);
        this.updatedAt = LocalDateTime.now();
    }

    // ── 도메인 규칙 ──────────────────────────────────────

    /** 현재 응답을 받을 수 있는 상태인지 확인 */
    public boolean isOpen() {
        return active && (closesAt == null || LocalDateTime.now().isBefore(closesAt));
    }
}
