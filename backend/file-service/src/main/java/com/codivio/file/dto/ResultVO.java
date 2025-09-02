package com.codivio.file.dto;

/**
 * 统一响应结果封装类
 */
public class ResultVO<T> {

    /**
     * 响应状态码
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
     * 请求是否成功
     */
    private Boolean success;

    /**
     * 时间戳
     */
    private Long timestamp;

    public ResultVO() {
        this.timestamp = System.currentTimeMillis();
    }

    public ResultVO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = code == 200;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应
     */
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "操作成功", data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "操作成功", null);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ResultVO<T> success(String message, T data) {
        return new ResultVO<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> ResultVO<T> error(String message) {
        return new ResultVO<>(500, message, null);
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> ResultVO<T> error(Integer code, String message) {
        return new ResultVO<>(code, message, null);
    }

    /**
     * 参数错误响应
     */
    public static <T> ResultVO<T> badRequest(String message) {
        return new ResultVO<>(400, message, null);
    }

    /**
     * 未授权响应
     */
    public static <T> ResultVO<T> unauthorized(String message) {
        return new ResultVO<>(401, message, null);
    }

    /**
     * 禁止访问响应
     */
    public static <T> ResultVO<T> forbidden(String message) {
        return new ResultVO<>(403, message, null);
    }

    /**
     * 资源不存在响应
     */
    public static <T> ResultVO<T> notFound(String message) {
        return new ResultVO<>(404, message, null);
    }

    // Getter and Setter methods
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
        this.success = code == 200;
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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ResultVO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", success=" + success +
                ", timestamp=" + timestamp +
                '}';
    }
}