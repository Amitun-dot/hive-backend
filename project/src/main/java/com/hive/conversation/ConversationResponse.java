package com.hive.conversation;

import java.time.LocalDateTime;

import com.hive.user.UserResponse;

public class ConversationResponse {

    private String id;
    private UserResponse participant;
    private LastMessageInfo lastMessage;
    private LocalDateTime updatedAt;

    public ConversationResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserResponse getParticipant() {
        return participant;
    }

    public void setParticipant(UserResponse participant) {
        this.participant = participant;
    }

    public LastMessageInfo getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LastMessageInfo lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class LastMessageInfo {
        private String content;
        private LocalDateTime createdAt;

        public LastMessageInfo() {
        }

        public LastMessageInfo(String content, LocalDateTime createdAt) {
            this.content = content;
            this.createdAt = createdAt;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
