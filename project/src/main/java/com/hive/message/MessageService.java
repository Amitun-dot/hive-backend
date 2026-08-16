package com.hive.message;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hive.conversation.Conversation;
import com.hive.conversation.ConversationRepository;
import com.hive.conversation.ConversationService;
import com.hive.exception.ForbiddenException;
import com.hive.exception.ResourceNotFoundException;
import com.hive.security.SecurityUtils;
import com.hive.user.User;
import com.hive.user.UserRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final ConversationService conversationService;

    public MessageService(MessageRepository messageRepository,
                          ConversationRepository conversationRepository,
                          UserRepository userRepository,
                          SecurityUtils securityUtils,
                          ConversationService conversationService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.conversationService = conversationService;
    }

    public List<MessageResponse> getMessages(String conversationId) {
        String currentUserId = securityUtils.getCurrentUserId();
        conversationService.verifyParticipant(conversationId, currentUserId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toResponse)
                .toList();
    }

    public MessageResponse sendMessage(String conversationId, String content) {
        String currentUserId = securityUtils.getCurrentUserId();
        conversationService.verifyParticipant(conversationId, currentUserId);

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setSenderUsername(sender.getUsername());
        message.setContent(content);
        message.setDeleted(false);
        message = messageRepository.save(message);

        Conversation conversation = conversationService.getConversationEntity(conversationId);
        conversation.setLastMessage(content);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return toResponse(message);
    }

    public void deleteMessage(String messageId) {
        String currentUserId = securityUtils.getCurrentUserId();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSenderId().equals(currentUserId)) {
            throw new ForbiddenException("You can only delete your own messages");
        }

        message.setDeleted(true);
        message.setContent(null);
        messageRepository.save(message);
    }

    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getSenderUsername(),
                message.getContent(),
                message.getCreatedAt(),
                message.isDeleted());
    }
}
