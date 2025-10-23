package com.e_commerce.e_commerce.exception;

import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.enums.ErrorCode;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalHandlerException {
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException appException) {
        log.info("in GlobalHandlerException - handleAppException");
        ErrorCode errorCode = appException.getErrorCode();
        return ResponseEntity.status(errorCode.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException accessDeniedException) {
        log.info("in GlobalHandlerException - handleAccessDeniedException");
        log.info("Exception type: {}, message: {}", accessDeniedException.getClass().getSimpleName(), accessDeniedException.getMessage());
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build()
        );
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.info("in GlobalHandlerException - handleException");
        log.info("Exception type: {}, message: {}", exception.getClass().getSimpleName(), exception.getMessage());
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        return ResponseEntity.status(errorCode.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        String enumKey = methodArgumentNotValidException.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.KEY_INVALID;
        Map<String, Object> attributes = null;
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        try {
            errorCode = ErrorCode.valueOf(enumKey);
            ConstraintViolation constraintViolation = methodArgumentNotValidException.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException e) {
        }
        if (Objects.nonNull(attributes)) {
            apiResponse.setMessage(mapAttribute(errorCode.getMessage(), attributes));
        }
        apiResponse.setCode(errorCode.getCode());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get("min"));
        return message.replace("{min}", minValue);
    }
}
