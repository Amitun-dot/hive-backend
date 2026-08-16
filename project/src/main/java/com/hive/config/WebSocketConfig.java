package com.hive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.hive.websocket.WebSocketAuthInterceptor;
import com.hive.websocket.WebSocketChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;
    private final WebSocketChannelInterceptor channelInterceptor;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor,
                            WebSocketChannelInterceptor channelInterceptor) {
        this.authInterceptor = authInterceptor;
        this.channelInterceptor = channelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins(frontendUrl)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }
}
