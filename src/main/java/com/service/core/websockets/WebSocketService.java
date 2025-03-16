package com.service.core.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.UserService;
import com.service.core.websockets.message.MessageStatus;
import com.service.core.websockets.message.MessageType;
import com.service.core.websockets.message.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    public void handleTextMessage(UUID userId, String textMessage) throws JsonProcessingException {

        WebSocketMessage request = objectMapper.readValue(textMessage, WebSocketMessage.class);

        switch (request.messageType()) {
            case AUTH_YANDEX_STATUS:
                handlerAuthYandexStatus(userId);
                break;
            default:
                logger.info("Unknown request type");
        }

    }

    private void handlerAuthYandexStatus(UUID userId) {
        WebSocketSession session = WebSocketHandler.sessions.get(userId);
        if (session == null || !session.isOpen()) {
            logger.info("Session for user " + userId + " not found or closed");
            return;
        }

        try {
            boolean success = userService.isUserOAuthSuccess(userId);
            WebSocketMessage response = success ?
                    WebSocketMessage.builder()
                            .messageType(MessageType.AUTH_YANDEX_STATUS)
                            .messageStatus(MessageStatus.SUCCESS)
                            .content("OAuth authorization success")
                            .build() :
                    WebSocketMessage.builder()
                            .messageType(MessageType.AUTH_YANDEX_STATUS)
                            .messageStatus(MessageStatus.ERROR)
                            .content("Error OAuth authorization, please try again")
                            .build();
            sendMessage(session, response);
        } catch (UserNotFoundException e) {
            WebSocketMessage response = WebSocketMessage.builder()
                    .messageType(MessageType.AUTH_YANDEX_STATUS)
                    .messageStatus(MessageStatus.ERROR)
                    .content(e.getMessage())
                    .build();
            sendMessage(session, response);
        }
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage response) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            logger.error("Error when sending a message: " + e.getMessage());
        }
    }
}
