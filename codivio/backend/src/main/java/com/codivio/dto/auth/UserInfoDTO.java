package com.codivio.dto.auth;

import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDTO {

    private Long userId;

    private String username;

    private String email;

    private String displayName;

    private String avatarUrl;

    private String bio;

    private Boolean emailVerified;

    private String createdAt;
}
