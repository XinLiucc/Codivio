package com.codivio.dto.auth;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponseDTO {

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private UserInfoDTO user;
}
