package com.codivio.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求DTO
 * 支持用户名或邮箱登录
 */
public class UserLoginDTO {
    
    /**
     * 登录标识：可以是用户名或邮箱
     */
    @NotBlank(message = "登录账号不能为空（请使用用户名或邮箱登录")
    @Size(min = 3, max = 50, message = "登录账号(长度必须在3-50个字符之间")
    private String loginId;
    
    /**
     * 登录密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    public UserLoginDTO() {
    }

    public UserLoginDTO(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserLoginDTO{" +
                "loginId='" + loginId + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}