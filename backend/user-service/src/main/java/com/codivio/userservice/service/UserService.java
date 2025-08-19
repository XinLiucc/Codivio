package com.codivio.userservice.service;

import com.codivio.userservice.dto.UserRegisterDTO;
import com.codivio.userservice.entity.User;

/**
 * 用户服务接口
 * 定义用户相关的业务操作
 */
public interface UserService {
    
    /**
     * 用户注册
     * 
     * 业务逻辑：
     * 1. 验证密码与确认密码是否一致
     * 2. 检查用户名是否已存在
     * 3. 检查邮箱是否已存在
     * 4. 加密密码
     * 5. 保存用户信息
     * 
     * @param registerDTO 注册数据 (已通过DTO格式验证)
     * @return 注册成功的用户信息
     * @throws RuntimeException 当业务规则验证失败时抛出异常
     */
    User register(UserRegisterDTO registerDTO);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return true-已存在, false-不存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱地址
     * @return true-已存在, false-不存在
     */
    boolean existsByEmail(String email);
}