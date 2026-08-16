package com.hive.user;

import java.time.LocalDateTime;

public class UserResponse {

    private String id;
    private String username;
    private String email;
    private Boolean online;
    private LocalDateTime lastSeen;

    public UserResponse() {
    }

    public UserResponse(String id, String username, String email, Boolean online, LocalDateTime lastSeen) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}
