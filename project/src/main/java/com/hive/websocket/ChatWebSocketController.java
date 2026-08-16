package com.hive.websocket;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.hive.conversation.Conversation;
import com.hive.conversation.ConversationRepository;
import com.hive.exception.ForbiddenException;
import com.hive.exception.ResourceNotFoundException;
import com.hive.message.Message;
import com.hive.message.MessageRepository;
import com.hive.message.MessageResponse;
import com.hive.message.MessageService;
import com.hive.user.User;
import com.hive.user.UserRepository;

import jakarta.validation.Valid;

@Controller
public class ChatWebSocketController {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    public ChatWebSocketController(MessageRepository messageRepository,
                                    ConversationRepository conversationRepository,
                                    UserRepository userRepository,
                                    SimpMessagingTemplate messagingTemplate,
                                    MessageService messageService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload @Valid WebSocketMessage webSocketMessage,
                            java.security.Principal principal) {
        String senderId = principal != null ? principal.getName() : null;
        if (senderId == null) {
            throw new ForbiddenException("Not authenticated");
        }

        Conversation conversation = conversationRepository.findById(webSocketMessage.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(senderId)) {
            throw new ForbiddenException("You do not have access to this conversation");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = new Message();
        message.setConversationId(webSocketMessage.getConversationId());
        message.setSenderId(senderId);
        message.setSenderUsername(sender.getUsername());
        message.setContent(webSocketMessage.getContent());
        message.setDeleted(false);
        message = messageRepository.save(message);

        conversation.setLastMessage(message.getContent());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageResponse response = messageService.toResponse(message);
        messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), response);
    }
}
