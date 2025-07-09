package com.codivio.dto.auth;

import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateUserRequestDTO {

    @Size(max = 100, message = "显示名称长度不能超过100个字符")
    private String displayName;

    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    private String avatarUrl;
}
