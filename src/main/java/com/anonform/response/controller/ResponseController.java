package com.anonform.response.controller;

import com.anonform.common.exception.DuplicateResponseException;
import com.anonform.common.exception.FormClosedException;
import com.anonform.common.response.ApiResponse;
import com.anonform.form.domain.Form;
import com.anonform.form.service.FormService;
import com.anonform.response.dto.SubmitFormRequest;
import com.anonform.response.dto.SubmitFormResponse;
import com.anonform.response.service.DuplicateResponseChecker;
import com.anonform.response.service.ResponseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/public/forms")
@RequiredArgsConstructor
public class ResponseController {

    private final FormService            formService;
    private final ResponseService        responseService;
    private final DuplicateResponseChecker duplicateChecker;

    /**
     * POST /api/v1/public/forms/{slug}/responses
     *
     * 처리 흐름:
     *  1. 폼 상태 검증 (활성화, 마감일)
     *  2. 최대 응답 수 검증
     *  3. 쿠키 토큰 기반 중복 응답 확인
     *  4. 응답 저장
     *  5. 응답 완료 토큰 캐싱 + HttpOnly 쿠키 발급
     */
    @PostMapping("/{slug}/responses")
    public ApiResponse<SubmitFormResponse> submitResponse(
            @PathVariable String slug,
            @Valid @RequestBody SubmitFormRequest request,
            @CookieValue(value = "response_token", required = false) String cookieToken,
            HttpServletResponse httpResponse) {

        // 1. 폼 상태 검증 (비활성 / 마감일 초과 시 FormClosedException)
        Form form = formService.getOpenForm(slug);

        // 2. 최대 응답 수 검증
        if (form.getMaxResponses() != null) {
            long count = responseService.countResponses(form.getId());
            if (count >= form.getMaxResponses()) {
                throw new FormClosedException("최대 응답 수에 도달했습니다.");
            }
        }

        // 3. 중복 응답 확인
        String responseToken = (cookieToken != null)
                ? cookieToken
                : duplicateChecker.issueResponseToken(form.getId());

        if (!form.isAllowDuplicate()
                && duplicateChecker.isDuplicate(form.getId(), responseToken)) {
            throw new DuplicateResponseException("이미 응답하셨습니다.");
        }

        // 4. 응답 저장
        responseService.saveResponse(form, request.answers(), responseToken);

        // 5. Redis 캐싱 + 쿠키 발급 (HttpOnly, Secure, SameSite=Lax, 30일)
        duplicateChecker.markAsResponded(responseToken);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(responseToken).toString());

        return ApiResponse.ok(new SubmitFormResponse("응답이 제출되었습니다."));
    }

    // ── private ──────────────────────────────────────────

    private ResponseCookie buildCookie(String token) {
        return ResponseCookie.from("response_token", token)
                .maxAge(Duration.ofDays(30))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .build();
    }
}
