package com.hive.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.hive.conversation.ConversationRepository;
import com.hive.security.JwtService;

import java.security.Principal;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final ConversationRepository conversationRepository;

    public WebSocketChannelInterceptor(JwtService jwtService, ConversationRepository conversationRepository) {
        this.jwtService = jwtService;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        if (command == null) return message;

        String token = extractTokenFromHeaders(accessor);
        if (token == null) return message;

        if (!jwtService.isTokenValid(token)) return message;

        String userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);

        if (accessor.getUser() == null) {
            accessor.setUser(new StompPrincipal(userId, username));
        }

        if (command == StompCommand.SUBSCRIBE) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/conversation/")) {
                String conversationId = destination.substring("/topic/conversation/".length());
                var conversation = conversationRepository.findById(conversationId);
                if (conversation.isEmpty() || !conversation.get().getParticipantIds().contains(userId)) {
                    return null;
                }
            }
        }

        return message;
    }

    private String extractTokenFromHeaders(StompHeaderAccessor accessor) {
        String auth = accessor.getFirstNativeHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    public record StompPrincipal(String userId, String username) implements Principal {
        @Override
        public String getName() {
            return userId;
        }
    }
}
