package com.hive.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hive.auth.LoginRequest;
import com.hive.auth.RegisterRequest;
import com.hive.conversation.ConversationRepository;
import com.hive.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getMessageHistory_success() throws Exception {
        String amitToken = setupUserAndConversation("amit", "rahul");
        String conversationId = getConversationId(amitToken, "rahul");

        mockMvc.perform(get("/api/conversations/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isOk());
    }

    @Test
    void deleteOwnMessage_success() throws Exception {
        String amitToken = setupUserAndConversation("amit", "rahul");
        String conversationId = getConversationId(amitToken, "rahul");

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(userRepository.findByUsername("amit").orElseThrow().getId());
        message.setSenderUsername("amit");
        message.setContent("Hello Rahul");
        message.setDeleted(false);
        message = messageRepository.save(message);

        mockMvc.perform(delete("/api/messages/" + message.getId())
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isNoContent());

        Message deleted = messageRepository.findById(message.getId()).orElseThrow();
        assert deleted.isDeleted() : "Message should be marked as deleted";
    }

    @Test
    void deleteOtherUserMessage_forbidden() throws Exception {
        String amitToken = setupUserAndConversation("amit", "rahul");
        String rahulToken = loginAndGetToken("rahul@example.com", "password123");
        String conversationId = getConversationId(amitToken, "rahul");

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(userRepository.findByUsername("amit").orElseThrow().getId());
        message.setSenderUsername("amit");
        message.setContent("Hello Rahul");
        message.setDeleted(false);
        message = messageRepository.save(message);

        mockMvc.perform(delete("/api/messages/" + message.getId())
                        .header("Authorization", "Bearer " + rahulToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteNonExistentMessage_notFound() throws Exception {
        String amitToken = setupUserAndConversation("amit", "rahul");

        mockMvc.perform(delete("/api/messages/nonexistent")
                        .header("Authorization", "Bearer " + amitToken))
                .andExpect(status().isNotFound());
    }

    private String setupUserAndConversation(String user1, String user2) throws Exception {
        registerUser(user1, user1 + "@example.com", "password123");
        registerUser(user2, user2 + "@example.com", "password123");
        return loginAndGetToken(user1 + "@example.com", "password123");
    }

    private String getConversationId(String token, String otherUsername) throws Exception {
        String otherId = userRepository.findByUsername(otherUsername).orElseThrow().getId();
        MvcResult result = mockMvc.perform(post("/api/conversations/" + otherId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
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
