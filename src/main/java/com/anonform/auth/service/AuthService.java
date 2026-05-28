package com.anonform.auth.service;

import com.anonform.auth.domain.User;
import com.anonform.auth.dto.LoginRequest;
import com.anonform.auth.dto.RegisterRequest;
import com.anonform.auth.dto.TokenResponse;
import com.anonform.auth.jwt.JwtTokenProvider;
import com.anonform.auth.repository.UserRepository;
import com.anonform.common.exception.DuplicateEmailException;
import com.anonform.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /** 이메일/비밀번호 회원가입 → JWT 즉시 발급 */
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }
        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );
        userRepository.save(user);
        return issueTokens(user.getId());
    }

    /** 이메일/비밀번호 로그인 */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return issueTokens(user.getId());
    }

    /** Refresh Token으로 새 토큰 쌍 발급 */
    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않거나 만료된 토큰입니다.");
        }
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        // TODO: Redis 블랙리스트 조회 (로그아웃된 토큰 차단)
        return issueTokens(userId);
    }

    /** OAuth2 로그인 시 CustomOAuth2UserService에서 호출 */
    public User findOrCreateOAuthUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.createOAuth(email, name)));
    }

    // ── private ──────────────────────────────────────────

    private TokenResponse issueTokens(UUID userId) {
        return TokenResponse.of(
                jwtTokenProvider.generateAccessToken(userId),
                jwtTokenProvider.generateRefreshToken(userId)
        );
    }
}
