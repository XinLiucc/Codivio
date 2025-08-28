package com.codivio.project.exception;

/**
 * 错误码枚举
 * 
 * 编码规则：XYZAB
 * - X: 错误级别 (1-系统级, 2-业务级)
 * - YZ: 服务编码 (01-用户服务, 02-项目服务, 03-协作服务, 04-文件服务, 05-网关服务)
 * - AB: 具体错误序号 (01-99)
 * 
 * @author Codivio Team
 * @since 1.0.0
 */
public enum ErrorCode {
    
    // ========== 通用成功和错误 ==========
    SUCCESS(200, "操作成功"),
    
    // ========== 系统级错误 (1xxxx) ==========
    SYSTEM_ERROR(10001, "系统内部错误"),
    DATABASE_ERROR(10002, "数据库操作失败"),
    NETWORK_ERROR(10003, "网络连接异常"),
    SERVICE_UNAVAILABLE(10004, "服务不可用"),
    
    // 用户服务系统级错误 (101xx)
    USER_SERVICE_ERROR(10101, "用户服务异常"),
    USER_DATABASE_ERROR(10102, "用户数据库操作失败"),
    
    // ========== 业务级错误 (2xxxx) ==========
    
    // 参数验证错误 (200xx)
    INVALID_PARAMETER(20001, "参数验证失败"),
    MISSING_REQUIRED_PARAMETER(20002, "缺少必填参数"),
    PARAMETER_FORMAT_ERROR(20003, "参数格式错误"),
    
    // 认证授权错误 (201xx)  
    UNAUTHORIZED(20101, "未授权访问"),
    TOKEN_INVALID(20102, "无效的访问令牌"),
    TOKEN_EXPIRED(20103, "访问令牌已过期"),
    PERMISSION_DENIED(20104, "权限不足"),
    
    // 用户服务业务错误 (202xx)
    // 用户注册相关 (2021x)
    USERNAME_ALREADY_EXISTS(20211, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(20212, "邮箱地址已存在"),
    PASSWORD_MISMATCH(20213, "密码与确认密码不一致"),
    INVALID_USERNAME_FORMAT(20214, "用户名格式不正确"),
    INVALID_EMAIL_FORMAT(20215, "邮箱格式不正确"),
    WEAK_PASSWORD(20216, "密码强度不足"),
    
    // 用户登录相关 (2022x)
    USER_NOT_FOUND(20221, "用户不存在"),
    INVALID_CREDENTIALS(20222, "用户名或密码错误"),
    USER_DISABLED(20223, "用户账户已被禁用"),
    LOGIN_FAILED(20224, "登录失败"),
    
    // 用户信息管理相关 (2023x)
    USER_UPDATE_FAILED(20231, "用户信息更新失败"),
    EMAIL_ALREADY_USED(20232, "邮箱已被其他用户占用"),
    AVATAR_UPLOAD_FAILED(20233, "头像上传失败"),
    
    // 项目服务业务错误 (203xx)
    PROJECT_NOT_FOUND(20301, "项目不存在"),
    PROJECT_ACCESS_DENIED(20302, "无项目访问权限"),
    PROJECT_NAME_ALREADY_EXISTS(20303, "项目名称已存在"),
    PROJECT_MEMBER_NOT_FOUND(20304, "项目成员不存在"),
    MEMBER_ALREADY_EXISTS(20305, "用户已是项目成员"),
    PROJECT_OWNER_CANNOT_LEAVE(20306, "项目所有者不能离开项目"),
    CANNOT_ADD_OWNER_ROLE(20307, "不能添加所有者角色"),
    CANNOT_REMOVE_OWNER(20308, "不能移除项目所有者"),
    CANNOT_MODIFY_OWNER_ROLE(20309, "不能修改项目所有者的角色"),
    
    // 文件服务业务错误 (204xx) - 为文件服务预留
    FILE_NOT_FOUND(20401, "文件不存在"),
    FILE_UPLOAD_FAILED(20402, "文件上传失败");
    
    /**
     * 错误码
     */
    private final int code;
    
    /**
     * 错误信息
     */
    private final String message;
    
    /**
     * 构造函数
     * 
     * @param code 错误码
     * @param message 错误信息
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取错误信息
     * 
     * @return 错误信息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据错误码查找对应的枚举
     * 
     * @param code 错误码
     * @return 对应的ErrorCode枚举，如果没找到则返回SYSTEM_ERROR
     */
    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
    
    /**
     * 检查是否为成功码
     * 
     * @return true-成功, false-失败
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * 检查是否为系统级错误
     * 
     * @return true-系统级错误, false-业务级错误
     */
    public boolean isSystemError() {
        return code >= 10000 && code < 20000;
    }
    
    /**
     * 检查是否为业务级错误
     * 
     * @return true-业务级错误, false-系统级错误
     */
    public boolean isBusinessError() {
        return code >= 20000 && code < 30000;
    }
}