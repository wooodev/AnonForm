package com.anonform.common.slug;

import com.anonform.form.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class SlugGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int    LENGTH   = 8;
    private static final int    MAX_RETRY = 10;

    private final SecureRandom   random = new SecureRandom();
    private final FormRepository formRepository;

    /** DB 중복 확인 후 유일한 슬러그 반환. 10회 재시도 초과 시 예외. */
    public String generateUnique() {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String candidate = randomString();
            if (!formRepository.existsBySlug(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("슬러그 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    private String randomString() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
