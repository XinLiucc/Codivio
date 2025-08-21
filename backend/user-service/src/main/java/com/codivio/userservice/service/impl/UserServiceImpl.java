package com.codivio.userservice.service.impl;

import com.codivio.userservice.dto.LoginResponseDTO;
import com.codivio.userservice.dto.UserLoginDTO;
import com.codivio.userservice.dto.UserRegisterDTO;
import com.codivio.userservice.entity.User;
import com.codivio.userservice.repository.UserRepository;
import com.codivio.userservice.service.UserService;
import com.codivio.userservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

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
    
    @Autowired
    private JwtUtil jwtUtil;
    
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
    
    /**
     * 用户登录
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(UserLoginDTO loginDTO) {
        // 1. 根据loginId查找用户（支持用户名或邮箱）
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(
            loginDTO.getLoginId(), 
            loginDTO.getLoginId()
        );
        
        // 2. 验证用户是否存在
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户名或邮箱不存在");
        }
        
        User user = userOptional.get();
        
        // 3. 验证密码是否正确
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }
        
        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 5. 清空敏感信息，准备返回给前端
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setEmail(user.getEmail());
        safeUser.setNickname(user.getNickname());
        safeUser.setStatus(user.getStatus());
        safeUser.setCreatedAt(user.getCreatedAt());
        safeUser.setUpdatedAt(user.getUpdatedAt());
        // 注意：不设置passwordHash
        
        // 6. 构造登录响应
        return new LoginResponseDTO(token, safeUser, jwtUtil.getExpiration());
    }

    /**
     * 根据用户ID查询用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息（已过滤敏感信息）
     * @throws RuntimeException 当用户不存在时抛出异常
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        // 1. 根据ID查询用户，JPA返回Optional避免空指针异常
        Optional<User> userOptional = userRepository.findById(userId);
        
        // 2. 检查用户是否存在
        if (userOptional.isEmpty()) {  // 使用isEmpty()更现代的写法
            throw new RuntimeException("用户不存在");
        }
        
        // 3. 获取用户对象
        User user = userOptional.get();
        
        // 4. 过滤敏感信息：清空密码哈希，确保不返回给前端
        user.setPasswordHash(null);
        
        return user;
    }
}