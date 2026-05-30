package com.anonform.response.service;

import com.anonform.response.repository.FormResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateResponseChecker {

    private static final String  CACHE_PREFIX = "response_token:";
    private static final Duration CACHE_TTL   = Duration.ofDays(30);

    private final StringRedisTemplate      redisTemplate;
    private final FormResponseRepository   formResponseRepository;

    /** 새 응답 토큰 발급 (formId prefix로 폼별 구분) */
    public String issueResponseToken(UUID formId) {
        return formId + ":" + UUID.randomUUID();
    }

    /**
     * 중복 응답 여부 확인.
     * Redis 캐시 → DB 순서로 확인 (캐시 히트 시 DB 쿼리 생략).
     */
    public boolean isDuplicate(UUID formId, String responseToken) {
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(CACHE_PREFIX + responseToken))) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Redis 연결 오류 — DB 폴백으로 중복 확인: {}", e.getMessage());
        }
        return formResponseRepository.existsByForm_IdAndResponseToken(formId, responseToken);
    }

    /**
     * 응답 완료 토큰을 Redis에 캐싱 (30일 TTL).
     * Redis 장애 시 로그만 남기고 계속 진행 (Fail-Open).
     */
    public void markAsResponded(String responseToken) {
        try {
            redisTemplate.opsForValue().set(CACHE_PREFIX + responseToken, "1", CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis 캐싱 실패 — DB 토큰으로만 중복 방지: {}", e.getMessage());
        }
    }
}
