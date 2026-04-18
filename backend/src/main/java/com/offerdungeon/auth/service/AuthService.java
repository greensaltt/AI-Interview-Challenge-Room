package com.offerdungeon.auth.service;

import com.offerdungeon.auth.model.AuthTokenResponse;
import com.offerdungeon.auth.model.AuthenticatedUser;
import com.offerdungeon.auth.model.CurrentUserResponse;
import com.offerdungeon.auth.model.LoginRequest;
import com.offerdungeon.auth.model.RegisterRequest;
import com.offerdungeon.auth.repository.AuthUserRecord;
import com.offerdungeon.auth.repository.AuthUserRepository;
import com.offerdungeon.common.exception.ApiErrorCode;
import com.offerdungeon.common.exception.BusinessException;
import com.offerdungeon.common.model.ApiErrorDetail;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE_CODE = "ROLE_USER";

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public CurrentUserResponse register(RegisterRequest request) {
        String username = normalizeRequired(request.username());
        String email = normalizeEmail(request.email());
        String nickname = normalizeNickname(request.nickname(), username);

        validateUniqueness(username, email);

        try {
            long userId = authUserRepository.createUser(
                    username, email, passwordEncoder.encode(request.password()), nickname);
            authUserRepository.assignRole(
                    userId, DEFAULT_ROLE_CODE, "Default role assigned during self-registration.");
            return CurrentUserResponse.from(loadAuthenticatedUser(userId));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ApiErrorCode.CONFLICT,
                    "Username or email is already registered.",
                    List.of(new ApiErrorDetail("account", "Please use a different username or email.")));
        }
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request, String loginIp) {
        String account = normalizeRequired(request.account());
        AuthUserRecord userRecord = authUserRepository.findByAccount(account)
                .orElseThrow(() -> new BusinessException(
                        ApiErrorCode.UNAUTHORIZED, "Invalid username/email or password."));

        ensureUserCanAuthenticate(userRecord);

        if (!passwordEncoder.matches(request.password(), userRecord.passwordHash())) {
            throw new BusinessException(
                    ApiErrorCode.UNAUTHORIZED, "Invalid username/email or password.");
        }

        authUserRepository.updateLastLogin(userRecord.id(), Instant.now(), loginIp);

        AuthenticatedUser authenticatedUser = loadAuthenticatedUser(userRecord.id());
        JwtTokenService.IssuedToken issuedToken = jwtTokenService.issueToken(authenticatedUser);

        return new AuthTokenResponse(
                issuedToken.token(),
                "Bearer",
                issuedToken.expiresAt().toString(),
                CurrentUserResponse.from(authenticatedUser));
    }

    public AuthenticatedUser loadAuthenticatedUser(Long userId) {
        AuthUserRecord userRecord = authUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ApiErrorCode.UNAUTHORIZED, "The user associated with this token no longer exists."));

        ensureUserCanAuthenticate(userRecord);

        return new AuthenticatedUser(
                userRecord.id(),
                userRecord.username(),
                userRecord.email(),
                userRecord.nickname(),
                userRecord.userStatus(),
                authUserRepository.findActiveRoleCodesByUserId(userRecord.id()));
    }

    private void validateUniqueness(String username, String email) {
        if (authUserRepository.existsByUsername(username)) {
            throw new BusinessException(
                    ApiErrorCode.CONFLICT,
                    "Username is already registered.",
                    List.of(new ApiErrorDetail("username", "Please choose another username.")));
        }
        if (authUserRepository.existsByEmail(email)) {
            throw new BusinessException(
                    ApiErrorCode.CONFLICT,
                    "Email is already registered.",
                    List.of(new ApiErrorDetail("email", "Please use another email address.")));
        }
    }

    private void ensureUserCanAuthenticate(AuthUserRecord userRecord) {
        switch (userRecord.userStatus()) {
            case "ACTIVE" -> {
                return;
            }
            case "DISABLED" -> throw new BusinessException(
                    ApiErrorCode.FORBIDDEN, "This account has been disabled.");
            case "LOCKED" -> throw new BusinessException(
                    ApiErrorCode.FORBIDDEN, "This account is locked.");
            case "DELETED" -> throw new BusinessException(
                    ApiErrorCode.FORBIDDEN, "This account has been deleted.");
            default -> throw new BusinessException(
                    ApiErrorCode.FORBIDDEN, "This account cannot be used for authentication.");
        }
    }

    private String normalizeRequired(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String value) {
        return normalizeRequired(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeNickname(String nickname, String username) {
        String normalizedNickname = nickname == null ? "" : nickname.trim();
        if (normalizedNickname.isBlank()) {
            return username;
        }
        return normalizedNickname;
    }
}
