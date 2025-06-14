package com.service.core.websockets.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.core.security.services.UserDetailsImpl;
import com.service.core.services.SessionService;
import com.service.core.websockets.message.FilmDecisionContent;
import com.service.core.websockets.message.MessageStatus;
import com.service.core.websockets.message.MessageType;
import com.service.core.websockets.message.WebSocketMessage;
import com.service.core.websockets.repositories.SessionRepositoryImpl;
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
public class PushDecisionHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(PushDecisionHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final SessionService sessionService;
    private final SessionRepositoryImpl sessionRepository;

    @Override
    public void handle(WebSocketSession session, WebSocketMessage message) {
        UUID userId = ((UserDetailsImpl) ((Authentication) session.getPrincipal()).getPrincipal()).getId();

        if (!session.isOpen()) {
            logger.info("Session for user " + userId + " not found or closed");
            return;
        }

        FilmDecisionContent filmDecision = objectMapper.convertValue(message.content(), FilmDecisionContent.class);

        var matchId = sessionService.saveDecision(userId, filmDecision.sessionId(), filmDecision.answer(), filmDecision.movieId());

        if(matchId.isPresent()) {
            var users = sessionService.getSessionUsers(filmDecision.sessionId());

            List<WebSocketSession> sessions = new ArrayList<>();
            users.forEach(u -> sessions.add(sessionRepository.get(u)));

            WebSocketMessage response = WebSocketMessage.builder()
                    .messageType(MessageType.MATCH_DETECTED)
                    .messageStatus(MessageStatus.SUCCESS)
                    .content(matchId.get())
                    .build();
            sessions.forEach(s -> sendMessage(s, response));

            sessionService.endSession(filmDecision.sessionId());
        }
    }
}
