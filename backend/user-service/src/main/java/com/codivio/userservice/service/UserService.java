package com.codivio.userservice.service;

import com.codivio.userservice.dto.LoginResponseDTO;
import com.codivio.userservice.dto.UserLoginDTO;
import com.codivio.userservice.dto.UserRegisterDTO;
import com.codivio.userservice.dto.UserUpdateDTO;
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
    
    /**
     * 验证用户ID和用户名是否匹配
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return true-匹配, false-不匹配或用户不存在
     */
    boolean validateUserMatch(Long userId, String username);
    
    /**
     * 用户登录
     * 
     * 业务逻辑：
     * 1. 根据loginId查找用户（支持用户名或邮箱）
     * 2. 验证用户是否存在
     * 3. 验证密码是否正确
     * 4. 生成JWT Token
     * 5. 返回Token和用户信息
     * 
     * @param loginDTO 登录数据 (已通过DTO格式验证)
     * @return 登录响应信息（包含token和用户信息）
     * @throws RuntimeException 当登录失败时抛出异常
     */
    LoginResponseDTO login(UserLoginDTO loginDTO);
    
    /**
     * 根据用户ID查询用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息（已过滤敏感信息）
     * @throws RuntimeException 当用户不存在时抛出异常
     */
    User getUserById(Long userId);
    
    /**
     * 更新用户信息
     * 
     * 业务逻辑：
     * 1. 验证用户是否存在
     * 2. 如果更新邮箱，检查新邮箱是否已被其他用户使用
     * 3. 只更新非空字段（部分更新）
     * 4. 自动更新updatedAt时间戳
     * 5. 返回更新后的用户信息
     * 
     * 支持的更新字段：
     * - email: 邮箱地址（需要验证唯一性）
     * - nickname: 用户昵称
     * - avatarUrl: 头像URL
     * 
     * @param userId 用户ID
     * @param userUpdateDTO 更新数据（只包含需要更新的字段）
     * @return 更新后的用户信息（已过滤敏感信息）
     * @throws RuntimeException 当用户不存在或邮箱重复时抛出异常
     */
    User updateUser(Long userId, UserUpdateDTO userUpdateDTO);
    
    /**
     * 根据用户ID查找用户（允许返回null）
     * 供服务间通信使用，不抛出异常
     * 
     * @param userId 用户ID
     * @return 用户信息，不存在时返回null
     */
    User findById(Long userId);
    
    /**
     * 根据用户名查找用户（允许返回null）
     * 供服务间通信使用，不抛出异常
     * 
     * @param username 用户名
     * @return 用户信息，不存在时返回null
     */
    User findByUsername(String username);
}