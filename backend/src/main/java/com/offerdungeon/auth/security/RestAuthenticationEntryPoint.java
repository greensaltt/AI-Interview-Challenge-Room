package com.offerdungeon.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offerdungeon.common.exception.ApiErrorCode;
import com.offerdungeon.common.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException)
            throws IOException {
        response.setStatus(ApiErrorCode.UNAUTHORIZED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.failure(ApiErrorCode.UNAUTHORIZED, resolveMessage(authenticationException)));
    }

    private String resolveMessage(AuthenticationException authenticationException) {
        String message = authenticationException.getMessage();
        if (message == null
                || message.isBlank()
                || message.startsWith("Full authentication is required")) {
            return "Authentication is required to access this resource.";
        }
        return message;
    }
}
