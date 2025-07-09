package com.codivio.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("email")
    private String email;

    @JsonIgnore
    @TableField("password_hash")
    private String passwordHash;

    @JsonIgnore
    @TableField("salt")
    private String salt;

    @TableField("display_name")
    private String displayName;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("bio")
    private String bio;

    /**
     * 状态: 0-禁用, 1-正常
     */
    @TableField("status")
    private Integer status;

    @TableField("email_verified")
    private Boolean emailVerified;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 常量定义
    public static class Status {
        public static final int DISABLED = 0;
        public static final int NORMAL = 1;
    }
}