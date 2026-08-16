package com.hive.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
    private String content;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content != null ? content.trim() : null;
    }
}
