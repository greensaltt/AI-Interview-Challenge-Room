package com.offerdungeon.common.exception;

import com.offerdungeon.common.model.ApiErrorDetail;
import java.util.List;

public class BusinessException extends RuntimeException {

    private final ApiErrorCode errorCode;
    private final List<ApiErrorDetail> errors;

    public BusinessException(String message) {
        this(ApiErrorCode.BUSINESS_ERROR, message, List.of());
    }

    public BusinessException(ApiErrorCode errorCode, String message) {
        this(errorCode, message, List.of());
    }

    public BusinessException(ApiErrorCode errorCode, String message, List<ApiErrorDetail> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }

    public List<ApiErrorDetail> getErrors() {
        return errors;
    }
}
