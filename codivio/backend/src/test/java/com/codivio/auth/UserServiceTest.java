package com.codivio.auth;

import com.codivio.service.UserService;
import com.codivio.dto.auth.RegisterRequestDTO;
import com.codivio.dto.auth.LoginRequestDTO;
import com.codivio.dto.auth.UserInfoDTO;
import com.codivio.dto.auth.LoginResponseDTO;
import com.codivio.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试类
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testUserRegistration() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setEmail("test@codivio.dev");
        request.setPassword("test123456");
        request.setDisplayName("测试用户");

        UserInfoDTO result = userService.register(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@codivio.dev", result.getEmail());
        assertEquals("测试用户", result.getDisplayName());
        assertFalse(result.getEmailVerified());
    }

    @Test
    public void testDuplicateUsernameRegistration() {
        RegisterRequestDTO request1 = new RegisterRequestDTO();
        request1.setUsername("duplicateuser");
        request1.setEmail("duplicate1@codivio.dev");
        request1.setPassword("test123456");
        userService.register(request1);

        RegisterRequestDTO request2 = new RegisterRequestDTO();
        request2.setUsername("duplicateuser");
        request2.setEmail("duplicate2@codivio.dev");
        request2.setPassword("test123456");

        assertThrows(BusinessException.class, () -> {
            userService.register(request2);
        });
    }

    @Test
    public void testUserLogin() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("loginuser");
        registerRequest.setEmail("login@codivio.dev");
        registerRequest.setPassword("login123456");
        userService.register(registerRequest);

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("login123456");

        LoginResponseDTO result = userService.login(loginRequest);

        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertEquals(86400L, result.getExpiresIn());
        assertNotNull(result.getUser());
        assertEquals("loginuser", result.getUser().getUsername());
    }

    @Test
    public void testInvalidLogin() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("nonexistent");
        request.setPassword("wrongpassword");

        assertThrows(BusinessException.class, () -> {
            userService.login(request);
        });
    }

    @Test
    public void testUsernameExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("existsuser");
        request.setEmail("exists@codivio.dev");
        request.setPassword("test123456");
        userService.register(request);

        assertTrue(userService.existsByUsername("existsuser"));
        assertFalse(userService.existsByUsername("nonexistent"));
    }

    @Test
    public void testEmailExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("emailuser");
        request.setEmail("emailtest@codivio.dev");
        request.setPassword("test123456");
        userService.register(request);

        assertTrue(userService.existsByEmail("emailtest@codivio.dev"));
        assertFalse(userService.existsByEmail("nonexistent@codivio.dev"));
    }
}
