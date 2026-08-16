package com.hive.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hive.auth.LoginRequest;
import com.hive.auth.RegisterRequest;
import com.hive.user.User;
import com.hive.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @BeforeEach
    void setUp() {
        conversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createConversation_success() throws Exception {
        registerUser("amit", "amit@example.com", "password123");
        registerUser("rahul", "rahul@example.com", "password123");
        String amitToken = loginAndGetToken("amit@example.com", "password123");
        String rahulId = userRepository.findByUsername("rahul").orElseThrow().getId();

        mockMvc.perform(post("/api/conversations/" + rahulId)
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.participant.username").value("rahul"));
    }

    @Test
    void createConversation_existingConversation_returnsSameConversation() throws Exception {
        registerUser("amit", "amit@example.com", "password123");
        registerUser("rahul", "rahul@example.com", "password123");
        String amitToken = loginAndGetToken("amit@example.com", "password123");
        String rahulId = userRepository.findByUsername("rahul").orElseThrow().getId();

        MvcResult first = mockMvc.perform(post("/api/conversations/" + rahulId)
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isOk())
                .andReturn();

        String firstId = objectMapper.readTree(first.getResponse().getContentAsString()).get("id").asText();

        MvcResult second = mockMvc.perform(post("/api/conversations/" + rahulId)
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isOk())
                .andReturn();

        String secondId = objectMapper.readTree(second.getResponse().getContentAsString()).get("id").asText();

        assert firstId.equals(secondId) : "Should return the same conversation";
    }

    @Test
    void getConversations_returnsUserConversations() throws Exception {
        registerUser("amit", "amit@example.com", "password123");
        registerUser("rahul", "rahul@example.com", "password123");
        registerUser("john", "john@example.com", "password123");
        String amitToken = loginAndGetToken("amit@example.com", "password123");
        String rahulId = userRepository.findByUsername("rahul").orElseThrow().getId();
        String johnId = userRepository.findByUsername("john").orElseThrow().getId();

        mockMvc.perform(post("/api/conversations/" + rahulId)
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/conversations/" + johnId)
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/conversations")
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getConversations_forbiddenForOtherUserConversation() throws Exception {
        registerUser("amit", "amit@example.com", "password123");
        registerUser("rahul", "rahul@example.com", "password123");
        registerUser("john", "john@example.com", "password123");
        String amitToken = loginAndGetToken("amit@example.com", "password123");
        String rahulToken = loginAndGetToken("rahul@example.com", "password123");
        String johnId = userRepository.findByUsername("john").orElseThrow().getId();

        MvcResult result = mockMvc.perform(post("/api/conversations/" + johnId)
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isOk())
                .andReturn();

        String conversationId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/conversations/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + rahulToken))
                .andExpect(status().isForbidden());
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

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
