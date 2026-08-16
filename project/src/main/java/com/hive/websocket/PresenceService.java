package com.hive.websocket;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.hive.user.User;
import com.hive.user.UserRepository;

@Service
public class PresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> onlineUserIds = ConcurrentHashMap.newKeySet();

    public PresenceService(UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void userConnected(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        user.setOnline(true);
        user.setLastSeen(null);
        userRepository.save(user);

        onlineUserIds.add(userId);

        PresenceEvent event = new PresenceEvent(user.getId(), user.getUsername(), true, null);
        messagingTemplate.convertAndSend("/topic/presence", event);
    }

    public void userDisconnected(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        onlineUserIds.remove(userId);

        user.setOnline(false);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        PresenceEvent event = new PresenceEvent(user.getId(), user.getUsername(), false, user.getLastSeen());
        messagingTemplate.convertAndSend("/topic/presence", event);
    }

    public boolean isUserOnline(String userId) {
        return onlineUserIds.contains(userId);
    }
}
