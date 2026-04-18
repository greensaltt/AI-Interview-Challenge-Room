package com.offerdungeon.common.exception;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {
    SUCCESS(HttpStatus.OK, "SUCCESS", "Request completed successfully."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Request could not be processed."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this resource."),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "The request conflicts with the current resource state."),
    BUSINESS_ERROR(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", "Business rule validation failed."),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "The system encountered an unexpected error.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ApiErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
