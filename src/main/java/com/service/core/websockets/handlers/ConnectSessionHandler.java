package com.service.core.websockets.handlers;

import com.service.core.models.dto.SessionDto;
import com.service.core.models.entities.Session;
import com.service.core.models.entities.UserSession;
import com.service.core.models.entities.UserSessionPK;
import com.service.core.models.entities.enums.ESessionStatus;
import com.service.core.repositories.SessionRepository;
import com.service.core.repositories.UserRepository;
import com.service.core.repositories.UserSessionRepository;
import com.service.core.security.services.UserDetailsImpl;

import com.service.core.services.SessionService;
import com.service.core.websockets.message.MessageStatus;
import com.service.core.websockets.message.MessageType;
import com.service.core.websockets.message.WebSocketMessage;
import com.service.core.websockets.repositories.SessionRepositoryImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectSessionHandler implements MessageHandler{
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final UserSessionRepository userSessionRepository;
    private final SessionService sessionService;
    private final SessionRepositoryImpl sessionRepositoryImpl;

    private final Logger logger = LoggerFactory.getLogger(ConnectSessionHandler.class);
    @Override
    @Transactional
    public void handle(WebSocketSession session, WebSocketMessage message) {
        UUID userId = ((UserDetailsImpl) ((Authentication) session.getPrincipal()).getPrincipal()).getId();

        if (!session.isOpen()) {
            logger.info("Session for user " + userId + " not found or closed");
            return;
        }

        String sessionId = objectMapper.convertValue(message.content(), String.class);
        var gameSession = sessionRepository.findById(sessionId).orElseThrow();
        var user = userRepository.findById(userId).orElseThrow();

        UserSessionPK userSessionPK = new UserSessionPK(user, gameSession);

        //if user already connected
        if (userSessionRepository.findById(userSessionPK).isPresent()) {
            WebSocketMessage response = WebSocketMessage.builder()
                    .messageType(MessageType.SESSION)
                    .messageStatus(MessageStatus.SUCCESS)
                    .content(getGameSession(userSessionPK.getSession()))
                    .build();
            sendMessage(session, response);
            return;
        }

        //if session in not new
        if (gameSession.getSessionStatus() != ESessionStatus.NEW) {
            WebSocketMessage response = WebSocketMessage.builder()
                    .messageType(MessageType.CONNECT_TO_SESSION)
                    .messageStatus(MessageStatus.ERROR)
                    .build();
            sendMessage(session, response);
            return;
        }

        UserSession userSession = new UserSession(new UserSessionPK(user, gameSession));
        Session resSession = userSessionRepository.save(userSession).getUserSessionPK().getSession();
        var users = sessionService.getSessionUsers(sessionId);
        List<WebSocketSession> sessions = new ArrayList<>();
        users.forEach(u -> sessions.add(sessionRepositoryImpl.get(u)));

        WebSocketMessage response = WebSocketMessage.builder()
                .messageType(MessageType.SESSION)
                .messageStatus(MessageStatus.SUCCESS)
                .content(getGameSession(resSession))
                .build();
        sessions.forEach(s -> sendMessage(s, response));
    }

    private SessionDto getGameSession(Session gameSession) {
        return SessionDto.builder()
                .id(gameSession.getId())
                .sessionStatus(gameSession.getSessionStatus())
                .moviesIds(gameSession.getMoviesIds())
                .usernames(userSessionRepository.findBySessionId(gameSession.getId()).stream().map(us -> us.getUserSessionPK().getUser().getUsername()).toList())
                .build();
    }
}
