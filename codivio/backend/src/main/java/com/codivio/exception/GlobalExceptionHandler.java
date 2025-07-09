package com.codivio.exception;

import com.codivio.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        Result<Object> result = Result.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode()).body(result);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Object>> handleValidationException(Exception e, HttpServletRequest request) {
        log.warn("参数验证异常: {} - {}", request.getRequestURI(), e.getMessage());

        BindingResult bindingResult;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else {
            bindingResult = ((BindException) e).getBindingResult();
        }

        StringBuilder errorMessage = new StringBuilder();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            errorMessage.append(fieldError.getDefaultMessage()).append("; ");
        }

        Result<Object> result = Result.error(400, errorMessage.toString());
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleGenericException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        Result<Object> result = Result.serverError("系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}