package com.service.core.websockets.handlers;

import com.service.core.security.services.UserDetailsImpl;
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
public class FetchKionPoiskIdHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(FetchKionPoiskIdHandler.class);
    private final UserService userService;
    @Override
    public void handle(WebSocketSession session, WebSocketMessage message) {
        UUID userId = ((UserDetailsImpl) ((Authentication) session.getPrincipal()).getPrincipal()).getId();

        if (!session.isOpen()) {
            logger.info("Session for user " + userId + " not found or closed");
            return;
        }

        try {
            userService.fetchKinoPoiskId(userId);
            WebSocketMessage response =
                    WebSocketMessage.builder()
                            .messageType(MessageType.FETCH_KINOPOISK_ID)
                            .messageStatus(MessageStatus.SUCCESS)
                            .content("KinoPoisk id successfully fetched!")
                            .build();
            sendMessage(session, response);
        } catch (Exception e) {
            WebSocketMessage response = WebSocketMessage.builder()
                    .messageType(MessageType.FETCH_KINOPOISK_ID)
                    .messageStatus(MessageStatus.ERROR)
                    .content(e.getMessage())
                    .build();
            sendMessage(session, response);
        }
    }
}
