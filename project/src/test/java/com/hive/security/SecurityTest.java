package com.hive.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hive.auth.LoginRequest;
import com.hive.auth.RegisterRequest;
import com.hive.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void unauthenticatedRequest_rejected() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedRequest_accepted() throws Exception {
        registerUser("amit", "amit@example.com", "password123");
        String token = loginAndGetToken("amit@example.com", "password123");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("amit"));
    }

    @Test
    void invalidToken_rejected() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    private void registerUser(String username, String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }
}
