package com.anonform.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    // OAuth2 로그인 사용자는 null
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(length = 20)
    @Builder.Default
    private String plan = "free";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── 팩토리 메서드 ──────────────────────────────────────

    /** 이메일/비밀번호 회원가입 */
    public static User create(String email, String encodedPassword, String name) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .build();
    }

    /** Google OAuth2 자동 가입 (비밀번호 없음) */
    public static User createOAuth(String email, String name) {
        return User.builder()
                .email(email)
                .name(name)
                .build();
    }
}
