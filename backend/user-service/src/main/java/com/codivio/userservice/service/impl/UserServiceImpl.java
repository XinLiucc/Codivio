package com.codivio.userservice.service.impl;

import com.codivio.userservice.dto.UserRegisterDTO;
import com.codivio.userservice.entity.User;
import com.codivio.userservice.repository.UserRepository;
import com.codivio.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     * 
     * @param registerDTO 注册数据
     * @return 注册成功的用户信息
     */
    @Override
    @Transactional
    public User register(UserRegisterDTO registerDTO) {
        // 1. 验证密码与确认密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("密码与确认密码不一致");
        }
        
        // 2. 检查用户名是否已存在
        if (existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 3. 检查邮箱是否已存在
        if (existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 4. 加密密码
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        
        // 5. 创建用户实体
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(encodedPassword);
        user.setNickname(registerDTO.getNickname());
        user.setStatus(1); // 默认状态：正常
        
        // 6. 保存用户到数据库
        return userRepository.save(user);
    }
    
    /**
     * 检查用户名是否已存在
     */
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否已存在
     */
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}