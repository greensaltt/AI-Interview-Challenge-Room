package com.offerdungeon.common.exception;

import com.offerdungeon.common.model.ApiErrorDetail;
import com.offerdungeon.common.model.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = "com.offerdungeon")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ApiErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode, exception.getMessage(), exception.getErrors()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        List<ApiErrorDetail> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .toList();
        return validationError(errors);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        List<ApiErrorDetail> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .toList();
        return validationError(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(
            HandlerMethodValidationException exception) {
        List<ApiErrorDetail> errors = exception.getParameterValidationResults().stream()
                .flatMap(result -> toParameterErrorDetails(result).stream())
                .toList();
        return validationError(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException exception) {
        List<ApiErrorDetail> errors = exception.getConstraintViolations().stream()
                .map(violation -> new ApiErrorDetail(
                        extractLeafNode(violation.getPropertyPath().toString()), violation.getMessage()))
                .toList();
        return validationError(errors);
    }

    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        List<ApiErrorDetail> errors = switch (exception) {
            case MissingServletRequestParameterException missingParameterException -> List.of(
                    new ApiErrorDetail(
                            missingParameterException.getParameterName(),
                            "Parameter is required."));
            case MethodArgumentTypeMismatchException typeMismatchException -> List.of(
                    new ApiErrorDetail(
                            typeMismatchException.getName(),
                            "Parameter type is invalid."));
            default -> List.of(new ApiErrorDetail("request", "Request body is invalid or unreadable."));
        };

        return ResponseEntity.status(ApiErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponse.failure(
                        ApiErrorCode.BAD_REQUEST, ApiErrorCode.BAD_REQUEST.getDefaultMessage(), errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unhandled exception caught by global exception handler.", exception);
        return ResponseEntity.status(ApiErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.failure(
                        ApiErrorCode.INTERNAL_SERVER_ERROR,
                        ApiErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage()));
    }

    private ResponseEntity<ApiResponse<Void>> validationError(List<ApiErrorDetail> errors) {
        return ResponseEntity.status(ApiErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(ApiResponse.failure(
                        ApiErrorCode.VALIDATION_ERROR,
                        ApiErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                        errors));
    }

    private ApiErrorDetail toFieldErrorDetail(FieldError fieldError) {
        return new ApiErrorDetail(fieldError.getField(), resolveMessage(fieldError));
    }

    private List<ApiErrorDetail> toParameterErrorDetails(ParameterValidationResult result) {
        String parameterName = result.getMethodParameter().getParameterName();
        return result.getResolvableErrors().stream()
                .map(error -> new ApiErrorDetail(parameterName, resolveMessage(error)))
                .collect(Collectors.toList());
    }

    private String resolveMessage(MessageSourceResolvable resolvable) {
        String defaultMessage = resolvable.getDefaultMessage();
        if (defaultMessage == null || defaultMessage.isBlank()) {
            return "Validation failed.";
        }
        return defaultMessage;
    }

    private String extractLeafNode(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return "request";
        }
        int separatorIndex = propertyPath.lastIndexOf('.');
        if (separatorIndex >= 0 && separatorIndex < propertyPath.length() - 1) {
            return propertyPath.substring(separatorIndex + 1);
        }
        return propertyPath;
    }
}
