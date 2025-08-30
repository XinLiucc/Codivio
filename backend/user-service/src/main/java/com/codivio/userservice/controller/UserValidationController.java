package com.codivio.userservice.controller;

import com.codivio.userservice.dto.ResultVO;
import com.codivio.userservice.dto.UserValidationDTO;
import com.codivio.userservice.entity.User;
import com.codivio.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户验证控制器
 * 专门用于服务间通信的用户验证接口
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserValidationController {

    @Autowired
    private UserService userService;
    
    /**
     * 验证用户是否存在（根据用户ID）
     * 供其他服务调用，验证用户ID的有效性
     * 
     * @param userId 用户ID
     * @return 用户验证结果
     */
    @GetMapping("/validate/{userId}")
    public ResultVO<UserValidationDTO> validateUser(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            if (user != null) {
                UserValidationDTO validation = UserValidationDTO.exists(
                    user.getId(), 
                    user.getUsername(), 
                    user.getEmail()
                );
                return ResultVO.success(validation);
            } else {
                return ResultVO.success(UserValidationDTO.notExists());
            }
        } catch (Exception e) {
            // 用户不存在或查询异常
            return ResultVO.success(UserValidationDTO.notExists());
        }
    }
    
    /**
     * 验证用户是否存在（根据用户名）
     * 供其他服务调用，验证用户名的有效性
     * 
     * @param username 用户名
     * @return 用户验证结果
     */
    @GetMapping("/validate-username/{username}")
    public ResultVO<UserValidationDTO> validateUserByUsername(@PathVariable String username) {
        try {
            User user = userService.findByUsername(username);
            if (user != null) {
                UserValidationDTO validation = UserValidationDTO.exists(
                    user.getId(), 
                    user.getUsername(), 
                    user.getEmail()
                );
                return ResultVO.success(validation);
            } else {
                return ResultVO.success(UserValidationDTO.notExists());
            }
        } catch (Exception e) {
            // 用户不存在或查询异常
            return ResultVO.success(UserValidationDTO.notExists());
        }
    }
}