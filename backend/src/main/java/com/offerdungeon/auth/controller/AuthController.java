package com.offerdungeon.auth.controller;

import com.offerdungeon.auth.model.AuthTokenResponse;
import com.offerdungeon.auth.model.AuthenticatedUser;
import com.offerdungeon.auth.model.CurrentUserResponse;
import com.offerdungeon.auth.model.LoginRequest;
import com.offerdungeon.auth.model.LogoutResponse;
import com.offerdungeon.auth.model.RegisterRequest;
import com.offerdungeon.auth.service.AuthService;
import com.offerdungeon.auth.support.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ClientIpResolver clientIpResolver;

    public AuthController(AuthService authService, ClientIpResolver clientIpResolver) {
        this.authService = authService;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/register")
    public CurrentUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthTokenResponse login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return authService.login(request, clientIpResolver.resolve(httpServletRequest));
    }

    @PostMapping("/logout")
    public LogoutResponse logout() {
        return new LogoutResponse(
                "Logout succeeded. This API is stateless, so please discard the access token on the client.");
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated user principal is unavailable.");
        }
        return CurrentUserResponse.from(user);
    }
}
