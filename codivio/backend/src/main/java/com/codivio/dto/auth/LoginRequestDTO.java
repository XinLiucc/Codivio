package com.codivio.dto.auth;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 */
@Data
public class LoginRequestDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
