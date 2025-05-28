package com.example.finalyearproject.Configs;

import com.example.finalyearproject.Security.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtHelper jwtHelper;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorization = accessor.getFirstNativeHeader("Authorization");

                    if (authorization != null && authorization.startsWith("Bearer ")) {
                        try {
                            String token = authorization.substring(7);

                            // Extract user email from token
                            String userEmail = jwtHelper.getUsernameFromToken(token);

                            // Extract role from token
                            String role = jwtHelper.getClaimFromToken(token, claims ->
                                    claims.get("role", String.class));

                            // Create authentication with proper authority
                            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    userEmail, null, Collections.singletonList(authority));

                            accessor.setUser(auth);

                            System.out.println("WebSocket authenticated: " + userEmail + " with role: " + role);
                        } catch (Exception e) {
                            System.err.println("WebSocket authentication failed: " + e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}