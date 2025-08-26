package com.codivio.userservice.dto;

/**
 * 统一响应结果封装类
 * 
 * @param <T> 响应数据的类型
 */
public class ResultVO<T> {
    
    /**
     * 响应状态码
     * 200: 成功
     * 400: 客户端错误
     * 500: 服务器错误
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    // 构造方法
    public ResultVO() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ResultVO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getter 和 Setter 方法
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    // ========== 静态工厂方法 ==========
    
    /**
     * 成功响应 (带数据)
     * 用法: ResultVO.success(user)
     */
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "操作成功", data);
    }
    
    /**
     * 成功响应 (仅消息)
     * 用法: ResultVO.success("注册成功")
     */
    public static <T> ResultVO<T> success(String message) {
        return new ResultVO<>(200, message, null);
    }
    
    /**
     * 失败响应 (默认400错误)
     * 用法: ResultVO.error("用户名已存在")
     */
    public static <T> ResultVO<T> error(String message) {
        return new ResultVO<>(400, message, null);
    }
    
    /**
     * 失败响应 (自定义错误码)
     * 用法: ResultVO.error(500, "服务器内部错误")
     */
    public static <T> ResultVO<T> error(Integer code, String message) {
        return new ResultVO<>(code, message, null);
    }
    
    /**
     * 失败响应 (自定义错误码 + 数据)
     * 用法: ResultVO.error(400, "参数验证失败", validationErrors)
     */
    public static <T> ResultVO<T> error(Integer code, String message, T data) {
        return new ResultVO<>(code, message, data);
    }
}