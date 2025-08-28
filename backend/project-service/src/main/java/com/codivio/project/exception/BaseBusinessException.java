package com.codivio.project.exception;

/**
 * 业务异常基类
 * 
 * 所有业务相关的异常都应该继承此类，而不是直接使用RuntimeException
 * 
 * @author Codivio Team
 * @since 1.0.0
 */
public class BaseBusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final ErrorCode errorCode;
    
    /**
     * 基础构造函数
     * 
     * @param errorCode 错误码
     */
    public BaseBusinessException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 支持自定义消息的构造函数
     * 
     * @param errorCode 错误码
     * @param message 自定义错误消息
     */
    public BaseBusinessException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 最完整的构造函数，支持异常链
     * 
     * @param errorCode 错误码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public BaseBusinessException(final ErrorCode errorCode, final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误码对象
     * 
     * @return 错误码枚举
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    
    /**
     * 获取错误码数字
     * 
     * @return 错误码
     */
    public int getCode() {
        return errorCode.getCode();
    }
}