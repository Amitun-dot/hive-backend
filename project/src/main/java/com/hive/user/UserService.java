package com.hive.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hive.security.SecurityUtils;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public UserService(UserRepository userRepository, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    public UserResponse getCurrentUser() {
        String currentUserId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
        return toResponse(user);
    }

    public List<UserResponse> getAllUsersExceptCurrent() {
        String currentUserId = securityUtils.getCurrentUserId();
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        return toResponse(user);
    }

    public User getEntityById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isOnline(),
                user.getLastSeen());
    }

    public UserResponse toResponseWithoutEmail(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                null,
                user.isOnline(),
                user.getLastSeen());
    }
}
