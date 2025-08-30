package com.codivio.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 用户服务客户端
 * 用于网关调用用户服务的内部接口
 */
@Component
public class UserServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceUrl;
    
    public UserServiceClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
    
    /**
     * 验证用户ID和用户名是否匹配
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return 验证结果
     */
    public Mono<Boolean> validateUser(Long userId, String username) {
        String url = userServiceUrl + "/api/v1/auth/validate-user/" + userId + "/" + username;
        
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(ResultVO.class)
                .map(result -> {
                    if (result.getCode() == 200) {
                        return (Boolean) result.getData();
                    }
                    return false;
                })
                .onErrorReturn(false);
    }
    
    /**
     * 结果包装类
     */
    public static class ResultVO<T> {
        private int code;
        private String message;
        private T data;
        private Long timestamp;
        
        // Getters and setters
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
}