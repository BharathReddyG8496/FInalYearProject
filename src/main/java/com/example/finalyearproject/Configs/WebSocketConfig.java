package com.example.finalyearproject.Configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker for sending messages to clients
        // Client subscribes to these destinations to receive messages
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws-chat" endpoint, enabling SockJS fallback options
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // For development; restrict in production
                .withSockJS(); // Enables SockJS fallback
    }
}