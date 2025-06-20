package com.service.core.websockets.handlers;

import com.service.core.models.entities.enums.ESessionStatus;
import com.service.core.repositories.SessionRepository;
import com.service.core.security.services.UserDetailsImpl;
import com.service.core.services.SessionService;
import com.service.core.websockets.message.MessageStatus;
import com.service.core.websockets.message.MessageType;
import com.service.core.websockets.message.WebSocketMessage;
import com.service.core.websockets.repositories.SessionRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartSessionHandler implements MessageHandler{
    private final SessionRepository sessionRepository;
    private final SessionService sessionService;
    private final SessionRepositoryImpl sessionRepositoryImpl;

    private final Logger logger = LoggerFactory.getLogger(ConnectSessionHandler.class);
    @Override
    @SneakyThrows
    public void handle(WebSocketSession session, WebSocketMessage message) {
        UUID userId = ((UserDetailsImpl) ((Authentication) session.getPrincipal()).getPrincipal()).getId();

        if (!session.isOpen()) {
            logger.info("Session for user " + userId + " not found or closed");
            return;
        }

        String sessionId = objectMapper.convertValue(message.content(), String.class);
        var gameSession = sessionRepository.findById(sessionId).orElseThrow();

        gameSession.setSessionStatus(ESessionStatus.RUN);

        var users = sessionService.getSessionUsers(sessionId);
        List<WebSocketSession> sessions = new ArrayList<>();
        users.forEach(u -> sessions.add(sessionRepositoryImpl.get(u)));

        WebSocketMessage response = WebSocketMessage.builder()
                .messageType(MessageType.SESSION)
                .messageStatus(MessageStatus.SUCCESS)
                .content(gameSession)
                .build();
        sessions.stream().filter(Objects::nonNull).forEach(s -> sendMessage(s, response));

        gameSession = sessionService.startSession(sessionId);
        var newResponse = WebSocketMessage.builder()
                .messageType(MessageType.SESSION)
                .messageStatus(MessageStatus.SUCCESS)
                .content(gameSession)
                .build();
        sessions.stream().filter(Objects::nonNull).forEach(s -> sendMessage(s, newResponse));
    }

}
