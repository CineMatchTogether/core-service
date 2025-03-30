package com.service.core.websockets.handlers;

import com.service.core.models.events.FetchingUserHistoryEvent;
import com.service.core.models.events.UserHistoryEvent;
import com.service.core.security.services.UserDetailsImpl;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.SettingService;
import com.service.core.services.UserMovieHistoryService;
import com.service.core.services.UserService;
import com.service.core.websockets.message.MessageStatus;
import com.service.core.websockets.message.MessageType;
import com.service.core.websockets.message.WebSocketMessage;
import com.service.core.websockets.repositories.SessionRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FetchKinoPoiskHistoryHandler implements MessageHandler {

    private final KafkaTemplate<UUID, FetchingUserHistoryEvent> kafkaTemplate;
    private final UserService userService;
    private final UserMovieHistoryService userMovieHistoryService;
    private final SettingService settingService;
    private final SessionRepositoryImpl sessionRepository;

    private static final Logger logger = LoggerFactory.getLogger(FetchKinoPoiskHistoryHandler.class);
    private static final String TOPIC = "fetch-history-request-topic";

    @Override
    public void handle(WebSocketSession session, WebSocketMessage message) {
        UUID userId = ((UserDetailsImpl) ((Authentication) session.getPrincipal()).getPrincipal()).getId();

        try {
            FetchingUserHistoryEvent event = new FetchingUserHistoryEvent(
                    userService.getOne(userId).getYandexAccount().getKinopoiskId().toString(),
                    settingService.getSetting().getCookie()
            );

            kafkaTemplate.send(TOPIC, userId, event);

        } catch (UserNotFoundException e) {
            WebSocketMessage response = WebSocketMessage.builder()
                    .messageType(MessageType.FETCH_KINOPOISK_HISTORY)
                    .messageStatus(MessageStatus.ERROR)
                    .content(e.getMessage())
                    .build();
            sendMessage(session, response);
        }
    }

    public void sendHistory(UUID userId, UserHistoryEvent event) {
        WebSocketSession session = sessionRepository.get(userId);

        if (session == null) {
            logger.error("Session for user {} not found", userId);
            return;
        }

        if (event.pageInfo().equals("Error fetching page")) {
            WebSocketMessage response = WebSocketMessage.builder()
                    .messageType(MessageType.FETCH_KINOPOISK_HISTORY)
                    .messageStatus(MessageStatus.ERROR)
                    .content("Error fetching page")
                    .build();
            sendMessage(session, response);
            return;
        }

        try {
            userMovieHistoryService.saveAll(userService.getOne(userId), event.filmIds());
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }

        WebSocketMessage response = WebSocketMessage.builder()
                .messageType(MessageType.FETCH_KINOPOISK_HISTORY)
                .messageStatus(MessageStatus.SUCCESS)
                .content(event)
                .build();

        sendMessage(session, response);
    }
}
