package com.example.finalyearproject.Configs;

import com.example.finalyearproject.Security.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtHelper jwtHelper; // Changed from JwtUtils to JwtHelper

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorization = accessor.getFirstNativeHeader("Authorization");
                    System.out.println("WebSocket Auth Header: " + authorization);

                    if (authorization != null && authorization.startsWith("Bearer ")) {
                        String token = authorization.substring(7);

                        // Extract user email from token - use the correct method from JwtHelper
                        String userEmail = jwtHelper.getUsernameFromToken(token);
                        System.out.println("WebSocket authenticated user: " + userEmail);

                        // THIS IS THE CRITICAL PART - set the user in the accessor
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userEmail, null, Collections.singletonList(new SimpleGrantedAuthority("USER")));
                        accessor.setUser(auth);
                    }
                }
                return message;
            }
        });
    }
}