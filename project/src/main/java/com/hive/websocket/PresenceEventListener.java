package com.hive.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceEventListener {

    private final PresenceService presenceService;

    public PresenceEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        var principal = event.getUser();
        if (principal != null) {
            presenceService.userConnected(principal.getName());
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        var principal = event.getUser();
        if (principal != null) {
            presenceService.userDisconnected(principal.getName());
        }
    }
}
