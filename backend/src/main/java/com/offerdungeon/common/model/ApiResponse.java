package com.offerdungeon.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.offerdungeon.common.exception.ApiErrorCode;
import com.offerdungeon.common.web.RequestTraceContext;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        String requestId,
        String timestamp,
        T data,
        List<ApiErrorDetail> errors) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                ApiErrorCode.SUCCESS.getCode(),
                ApiErrorCode.SUCCESS.getDefaultMessage(),
                RequestTraceContext.getRequestId(),
                Instant.now().toString(),
                data,
                null);
    }

    public static ApiResponse<Void> failure(ApiErrorCode errorCode, String message) {
        return failure(errorCode, message, List.of());
    }

    public static ApiResponse<Void> failure(
            ApiErrorCode errorCode, String message, List<ApiErrorDetail> errors) {
        return new ApiResponse<>(
                false,
                errorCode.getCode(),
                message,
                RequestTraceContext.getRequestId(),
                Instant.now().toString(),
                null,
                errors == null || errors.isEmpty() ? null : List.copyOf(errors));
    }
}
