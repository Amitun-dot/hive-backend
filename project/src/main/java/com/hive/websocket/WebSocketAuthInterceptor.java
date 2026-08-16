package com.hive.websocket;

import java.security.Principal;
import java.util.List;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.hive.security.CustomUserDetails;
import com.hive.security.CustomUserDetailsService;
import com.hive.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = extractToken(httpRequest);

            if (token != null && jwtService.isTokenValid(token)) {
                String userId = jwtService.extractUserId(token);
                try {
                    var userDetails = (CustomUserDetails) userDetailsService.loadUserById(userId);
                    attributes.put("userId", userDetails.getId());
                    attributes.put("username", userDetails.getDisplayName());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isBlank()) {
            return queryToken;
        }
        return null;
    }
}
