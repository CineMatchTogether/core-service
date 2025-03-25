package com.service.core.websockets.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.core.websockets.handlers.AuthYandexHandler;
import com.service.core.websockets.handlers.FetchKinoPoiskHistoryHandler;
import com.service.core.websockets.message.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
public class TextMessageHandler {

    private final ObjectMapper objectMapper;
    private final AuthYandexHandler authYandexHandler;
    private final FetchKinoPoiskHistoryHandler fetchKinoPoiskHistoryHandler;
    private static final Logger logger = LoggerFactory.getLogger(TextMessageHandler.class);

    public void handleTextMessage(WebSocketSession session, String message) throws JsonProcessingException {

        WebSocketMessage request = objectMapper.readValue(message, WebSocketMessage.class);

        switch (request.messageType()) {
            case AUTH_YANDEX_STATUS:
                authYandexHandler.handle(session, request);
                break;
            default:
                logger.info("Unknown request type");
        }

    }
}
