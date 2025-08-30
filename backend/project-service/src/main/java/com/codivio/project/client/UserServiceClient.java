package com.codivio.project.client;

import com.codivio.project.dto.ResultVO;
import com.codivio.project.dto.UserValidationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 * 用于项目服务调用用户服务的接口
 */
@FeignClient(
    name = "user-service", 
    url = "http://localhost:8081"
)
public interface UserServiceClient {
    
    /**
     * 验证用户是否存在
     * 
     * @param userId 用户ID
     * @return 用户验证结果
     */
    @GetMapping("/api/v1/users/validate/{userId}")
    ResultVO<UserValidationDTO> validateUser(@PathVariable(value = "userId") Long userId);
    
    /**
     * 根据用户名验证用户是否存在
     * 
     * @param username 用户名
     * @return 用户验证结果
     */
    @GetMapping("/api/v1/users/validate-username/{username}")
    ResultVO<UserValidationDTO> validateUserByUsername(@PathVariable(value = "username") String username);
}