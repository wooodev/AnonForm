package com.anonform.common.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RESPONSE_PATTERN = "/api/v1/public/forms/{slug}/responses";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final StringRedisTemplate redisTemplate;

    @Value("${rate-limit.response-submit.max-requests:5}")
    private int maxRequests;

    @Value("${rate-limit.response-submit.window-seconds:60}")
    private int windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 응답 제출 POST 요청에만 적용
        if (!"POST".equalsIgnoreCase(request.getMethod())
                || !PATH_MATCHER.match(RESPONSE_PATTERN, request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String slug = extractSlug(request.getRequestURI());
        String ip   = resolveClientIp(request);
        String key  = "rate_limit:" + ip + ":" + slug;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            if (count != null && count > maxRequests) {
                writeTooManyRequests(response);
                return;
            }
        } catch (Exception e) {
            // Redis 장애 시 Fail-Open — 요청 허용 후 경고 로그
            log.warn("Rate limit Redis 오류 — 요청 허용: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    // ── private ──────────────────────────────────────────

    private String extractSlug(String uri) {
        Map<String, String> vars = PATH_MATCHER.extractUriTemplateVariables(
                RESPONSE_PATTERN, uri);
        return vars.getOrDefault("slug", "unknown");
    }

    /** X-Forwarded-For 우선, 없으면 remoteAddr */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"data\":null,"
                + "\"message\":\"너무 많은 요청입니다. 잠시 후 다시 시도해주세요.\"}");
    }
}
