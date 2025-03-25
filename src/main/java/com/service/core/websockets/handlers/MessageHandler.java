package com.service.core.websockets.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.core.websockets.message.WebSocketMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface MessageHandler {

    ObjectMapper objectMapper = new ObjectMapper();

    void handle(WebSocketSession session, WebSocketMessage message);

    default void sendMessage(WebSocketSession session, WebSocketMessage response) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            throw new RuntimeException("Error when sending a message: " + e.getMessage());
        }
    }
}
