package com.hive.message;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hive.conversation.ConversationService;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    public MessageController(MessageService messageService, ConversationService conversationService) {
        this.messageService = messageService;
        this.conversationService = conversationService;
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable String conversationId) {
        return ResponseEntity.ok(messageService.getMessages(conversationId));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
