package com.hive.conversation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        return ResponseEntity.ok(conversationService.getConversationsForCurrentUser());
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ConversationResponse> createOrGetConversation(@PathVariable String userId) {
        return ResponseEntity.ok(conversationService.createOrGetConversation(userId));
    }
}
