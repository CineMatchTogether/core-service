package com.service.core.websockets.handlers;

import com.service.core.security.services.UserDetailsImpl;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.UserService;
import com.service.core.websockets.message.MessageStatus;
import com.service.core.websockets.message.MessageType;
import com.service.core.websockets.message.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthYandexHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthYandexHandler.class);
    private final UserService userService;

    @Override
    public void handle(WebSocketSession session, WebSocketMessage message) {
        UUID userId = ((UserDetailsImpl) ((Authentication) session.getPrincipal()).getPrincipal()).getId();

        if (!session.isOpen()) {
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
}
