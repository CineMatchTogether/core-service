package com.service.core.websockets.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.core.websockets.handlers.*;
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
    private final FetchKionPoiskIdHandler fetchKionPoiskIdHandler;
    private final FetchKinoPoiskHistoryHandler fetchKinoPoiskHistoryHandler;
    private final PushDecisionHandler pushDecisionHandler;
    private final ConnectSessionHandler connectSessionHandler;
    private final StartSessionHandler startSessionHandler;
    private static final Logger logger = LoggerFactory.getLogger(TextMessageHandler.class);

    public void handleTextMessage(WebSocketSession session, String message) throws JsonProcessingException {

        WebSocketMessage request = objectMapper.readValue(message, WebSocketMessage.class);

        switch (request.messageType()) {
            case FETCH_KINOPOISK_ID:
                fetchKionPoiskIdHandler.handle(session, request);
                break;
            case FETCH_KINOPOISK_HISTORY:
                fetchKinoPoiskHistoryHandler.handle(session, request);
                break;
            case PUSH_DECISION:
                pushDecisionHandler.handle(session, request);
                break;
            case CONNECT_TO_SESSION:
                connectSessionHandler.handle(session, request);
                break;
            case START_SESSION:
                startSessionHandler.handle(session, request);
                break;


            default:
                logger.info("Unknown request type");
        }

    }
}
