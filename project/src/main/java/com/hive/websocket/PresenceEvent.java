package com.hive.websocket;

import java.time.LocalDateTime;

public class PresenceEvent {

    private String userId;
    private String username;
    private boolean online;
    private LocalDateTime lastSeen;

    public PresenceEvent() {
    }

    public PresenceEvent(String userId, String username, boolean online, LocalDateTime lastSeen) {
        this.userId = userId;
        this.username = username;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}
