package com.service.core.websockets;

import com.service.core.security.services.UserDetailsImpl;
import com.service.core.websockets.repositories.SessionRepository;
import com.service.core.websockets.services.TextMessageHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.Principal;
import java.util.UUID;

@Getter
@Service
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
    private final SessionRepository<UUID, WebSocketSession> sessionRepository;

    private final TextMessageHandler textMessageHandler;

    private final static Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Principal principal = session.getPrincipal();

        Authentication authentication = (Authentication) principal;
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
        UUID userId = userDetailsImpl.getId();

        sessionRepository.put(userId, session);
        System.out.println("Новое соединение: " + session.getId() + " " + userId);
        session.sendMessage(new TextMessage("Соединение установлено"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Получено сообщение: " + payload);
        System.out.println(session.getPrincipal().getName());
        textMessageHandler.handleTextMessage(session, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UUID userId = ((UserDetailsImpl) ((Authentication) session.getPrincipal()).getPrincipal()).getId();
        sessionRepository.remove(userId);
        logger.info("Connection closed: " + userId + " " + status.toString());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error: " + exception.getMessage());
        UUID userId = ((UserDetailsImpl) ((Authentication) session.getPrincipal()).getPrincipal()).getId();
        sessionRepository.remove(userId);
    }

}
