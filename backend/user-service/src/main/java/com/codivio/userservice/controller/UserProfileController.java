package com.codivio.userservice.controller;

import com.codivio.userservice.dto.ResultVO;
import com.codivio.userservice.dto.UserUpdateDTO;
import com.codivio.userservice.entity.User;
import com.codivio.userservice.exception.BaseBusinessException;
import com.codivio.userservice.exception.ErrorCode;
import com.codivio.userservice.service.UserService;
import com.codivio.userservice.util.GatewayUserUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * 用户信息管理控制器
 * 
 * 功能范围：
 * - 用户信息查询
 * - 用户信息更新（待实现）
 * - 用户头像管理（待实现）
 * 
 * 安全说明：
 * - 所有接口都需要JWT认证
 * - 用户只能访问自己的信息
 * - 通过Spring Security保护，无需手动验证token
 * 
 * RESTful设计：
 * - 基础路径：/api/v1/users
 * - 遵循用户资源管理的REST规范
 * 
 * @author Codivio Team
 * @since 2025-08-21
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private GatewayUserUtil gatewayUserUtil;

    @Value("${avatar.upload.dir:./data/avatars}")
    private String avatarUploadDir;
    
    /**
     * 获取当前用户信息
     * 
     * 接口说明：
     * - URL: GET /api/v1/users/profile
     * - 需要JWT认证：通过Authorization header传递Bearer token
     * - 返回当前登录用户的详细信息（已过滤敏感信息）
     * 
     * 实现原理：
     * 1. Spring Security的JwtAuthenticationFilter已验证token
     * 2. 从Security上下文获取当前用户ID（无需重复验证token）
     * 3. 调用Service层查询用户详细信息
     * 4. 返回统一格式的响应
     * 
     * 安全保证：
     * - 只能查询当前登录用户的信息
     * - 密码等敏感信息已在Service层过滤
     * 
     * @return 当前用户信息的统一响应格式
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功", 
     *   "data": {
     *     "id": 1,
     *     "username": "testuser",
     *     "email": "test@example.com",
     *     "nickname": "Test User",
     *     "status": 1,
     *     "createdAt": "2025-08-21T10:30:00",
     *     "updatedAt": "2025-08-21T10:30:00"
     *   },
     *   "timestamp": 1640995200000
     * }
     */
    @GetMapping("/profile")
    public ResultVO<User> getCurrentUserProfile() {
        // 1. 从网关传递的请求头获取用户ID
        Long userId = gatewayUserUtil.getCurrentUserId();
        
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED, "无法获取用户信息，请通过网关访问");
        }

        // 2. 通过Service层查询用户信息
        User user = userService.getUserById(userId);

        // 3. 返回成功响应
        return ResultVO.success(user);
    }

    /**
     * 更新当前用户信息
     * 
     * 接口说明：
     * - URL: PUT /api/v1/users/profile
     * - 需要JWT认证：通过Authorization header传递Bearer token
     * - 支持部分更新：只更新请求中提供的字段
     * - 返回更新后的用户信息（已过滤敏感信息）
     * 
     * 支持更新的字段：
     * - email: 邮箱地址（会验证格式和唯一性）
     * - nickname: 用户昵称（1-50字符）
     * - avatarUrl: 头像URL（可为空）
     * 
     * 实现原理：
     * 1. Spring Security已验证JWT token并设置认证上下文
     * 2. 从认证上下文获取当前用户ID
     * 3. 调用Service层进行业务验证和数据更新
     * 4. 返回更新后的用户信息
     * 
     * 安全保证：
     * - 用户只能更新自己的信息
     * - 邮箱唯一性验证
     * - 敏感信息已过滤
     * 
     * @param userUpdateDTO 更新数据（经过Bean Validation验证）
     * @return 更新后的用户信息
     * 
     * 请求示例：
     * {
     *   "email": "newemail@example.com",
     *   "nickname": "New Nickname"
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "id": 1,
     *     "username": "testuser",
     *     "email": "newemail@example.com",
     *     "nickname": "New Nickname",
     *     "status": 1,
     *     "updatedAt": "2025-08-21T15:30:00"
     *   },
     *   "timestamp": 1640995200000
     * }
     */
    @PutMapping("/profile")
    public ResultVO<User> updateCurrentUserProfile(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        // 1. 从网关传递的请求头获取用户ID
        Long userId = gatewayUserUtil.getCurrentUserId();
        
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED, "无法获取用户信息，请通过网关访问");
        }

        // 2. 调用Service层执行更新操作
        User updatedUser = userService.updateUser(userId, userUpdateDTO);

        // 3. 返回更新后的用户信息
        return ResultVO.success(updatedUser);
    }

    /**
     * 上传头像
     * POST /api/v1/users/avatar
     */
    @PostMapping("/avatar")
    public ResultVO<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/gif"))) {
            throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, "仅支持 JPG、PNG、GIF 格式");
        }

        // 确保目录存在
        Path uploadPath = Paths.get(avatarUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成文件名：userId + 随机后缀，防止缓存问题
        String ext = contentType.equals("image/png") ? ".png" : contentType.equals("image/gif") ? ".gif" : ".jpg";
        String filename = userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path filePath = uploadPath.resolve(filename);

        // 删除该用户旧头像
        Files.list(uploadPath)
                .filter(p -> p.getFileName().toString().startsWith(userId + "_"))
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });

        // 保存文件
        file.transferTo(filePath.toFile());

        // 更新 DB 中的 avatar_url
        String avatarUrl = "/api/v1/users/avatar/" + filename;
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setAvatarUrl(avatarUrl);
        userService.updateUser(userId, updateDTO);

        return ResultVO.success(Map.of("avatarUrl", avatarUrl));
    }

    /**
     * 访问头像文件
     * GET /api/v1/users/avatar/{filename}
     */
    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) throws IOException {
        // 防止路径穿越攻击
        if (filename.contains("..") || filename.contains("/")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = Paths.get(avatarUploadDir).resolve(filename);
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "image/jpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
