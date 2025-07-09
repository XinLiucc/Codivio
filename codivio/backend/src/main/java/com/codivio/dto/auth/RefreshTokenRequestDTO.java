package com.codivio.dto.auth;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新Token请求DTO
 */
@Data
public class RefreshTokenRequestDTO {

    @NotBlank(message = "刷新Token不能为空")
    private String refreshToken;
}
