package com.codivio.auth;

import com.codivio.dto.auth.RegisterRequestDTO;
import com.codivio.dto.auth.LoginRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器测试类
 */
@TestPropertySource(properties = "server.servlet.context-path=/api/v1")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterEndpoint() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("apitest");
        request.setEmail("apitest@codivio.dev");
        request.setPassword("test123456");
        request.setDisplayName("API测试用户");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.username").value("apitest"));
    }

    @Test
    public void testLoginEndpoint() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("loginapi");
        registerRequest.setEmail("loginapi@codivio.dev");
        registerRequest.setPassword("test123456");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("loginapi");
        loginRequest.setPassword("test123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("loginapi"));
    }

    @Test
    public void testCheckUsernameEndpoint() throws Exception {
        mockMvc.perform(get("/auth/check-username")
                        .param("username", "available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void testInvalidRegistration() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername(""); // 空用户名
        request.setEmail("invalid-email"); // 错误邮箱格式
        request.setPassword("123"); // 密码太短

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
