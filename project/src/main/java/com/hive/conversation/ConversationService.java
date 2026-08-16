package com.hive.conversation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hive.exception.ForbiddenException;
import com.hive.exception.ResourceNotFoundException;
import com.hive.message.Message;
import com.hive.message.MessageRepository;
import com.hive.security.SecurityUtils;
import com.hive.user.User;
import com.hive.user.UserRepository;
import com.hive.user.UserResponse;
import com.hive.user.UserService;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SecurityUtils securityUtils;
    private final UserService userService;

    public ConversationService(
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            MessageRepository messageRepository,
            SecurityUtils securityUtils,
            UserService userService) {

        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.securityUtils = securityUtils;
        this.userService = userService;
    }

    public ConversationResponse createOrGetConversation(String otherUserId) {

        String currentUserId = securityUtils.getCurrentUserId();

        // Prevent chatting with yourself
        if (currentUserId.equals(otherUserId)) {
            throw new ForbiddenException(
                    "Cannot create a conversation with yourself");
        }

        // Make sure the other user exists
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        /*
         * Find an existing conversation containing BOTH users.
         *
         * We use a custom MongoDB $all query because using:
         *
         * findByParticipantIdsContainingAndParticipantIdsContaining(...)
         *
         * causes Spring Data MongoDB to create duplicate participantIds
         * expressions and throws InvalidMongoDbApiUsageException.
         */
        Optional<Conversation> existing =
                conversationRepository.findByBothParticipantIds(
                        currentUserId,
                        otherUserId
                );

        Conversation conversation;

        if (existing.isPresent()) {

            // Conversation already exists
            conversation = existing.get();

        } else {

            // Create a new conversation
            conversation = new Conversation();

            conversation.setParticipantIds(
                    List.of(currentUserId, otherUserId)
            );

            conversation = conversationRepository.save(conversation);
        }

        return toResponse(conversation, currentUserId);
    }

    public List<ConversationResponse> getConversationsForCurrentUser() {

        String currentUserId = securityUtils.getCurrentUserId();

        return conversationRepository
                .findByParticipantIdsContaining(currentUserId)
                .stream()
                .map(conversation ->
                        toResponse(conversation, currentUserId))
                .toList();
    }

    public Conversation getConversationEntity(String conversationId) {

        return conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found"));
    }

    public void verifyParticipant(
            String conversationId,
            String userId) {

        Conversation conversation =
                getConversationEntity(conversationId);

        if (!conversation.getParticipantIds().contains(userId)) {

            throw new ForbiddenException(
                    "You do not have access to this conversation");
        }
    }

    public ConversationResponse toResponse(
            Conversation conversation,
            String currentUserId) {

        String otherUserId = conversation
                .getParticipantIds()
                .stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Conversation has no other participant"));

        User otherUser = userRepository
                .findById(otherUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Participant user not found"));

        UserResponse participant =
                userService.toResponse(otherUser);

        ConversationResponse response =
                new ConversationResponse();

        response.setId(conversation.getId());
        response.setParticipant(participant);
        response.setUpdatedAt(conversation.getUpdatedAt());

        /*
         * Get the latest message.
         */
        if (conversation.getLastMessage() != null) {

            List<Message> messages =
                    messageRepository
                            .findByConversationIdOrderByCreatedAtAsc(
                                    conversation.getId());

            if (!messages.isEmpty()) {

                Message last =
                        messages.get(messages.size() - 1);

                response.setLastMessage(
                        new ConversationResponse.LastMessageInfo(
                                last.getContent(),
                                last.getCreatedAt()
                        )
                );
            }
        }

        return response;
    }
}