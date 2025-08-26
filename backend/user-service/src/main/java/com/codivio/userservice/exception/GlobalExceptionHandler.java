package com.codivio.userservice.exception;

import com.codivio.userservice.dto.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 
 * 统一处理所有Controller层抛出的异常，确保返回给前端的错误格式一致
 * 
 * @author Codivio Team
 * @since 1.0.0
 */
@ControllerAdvice  // 👈 告诉Spring这是全局异常处理器
@ResponseBody      // 👈 所有方法都返回JSON格式
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     * 
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BaseBusinessException.class)
    public ResultVO<Void> handleBusinessException (BaseBusinessException e) {
        logger.warn("业务异常:[{}] {}", e.getCode(), e.getMessage());
        return ResultVO.error(e.getCode(), e.getMessage());
    }
    
    
    /**
     * 处理参数验证异常
     * 
     * @param e 参数验证异常
     * @return 包含验证错误详情的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("参数验证异常: {}", e.getMessage());
        
        // 提取验证错误信息
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResultVO.error(ErrorCode.INVALID_PARAMETER.getCode(),
                             ErrorCode.INVALID_PARAMETER.getMessage(),
                             errors);
    }
    
    
    /**
     * 处理运行时异常
     * 
     * @param e 运行时异常
     * @return 系统错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ResultVO<Void> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: ", e);  // 👈 记录完整堆栈信息
        return ResultVO.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }
    
    /**
     * 处理所有其他异常（兜底处理）
     * 
     * @param e 未知异常
     * @return 系统错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        logger.error("未知异常: ", e);  // 👈 记录完整堆栈信息
        return ResultVO.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }
}