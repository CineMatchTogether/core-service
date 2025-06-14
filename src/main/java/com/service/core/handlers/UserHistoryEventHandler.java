package com.service.core.handlers;

import com.service.core.models.events.UserHistoryEvent;
import com.service.core.websockets.handlers.FetchKinoPoiskHistoryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "user-history-topic", groupId = "user-history-events", containerFactory = "userHistoryKafkaListenerContainerFactory")
public class UserHistoryEventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final FetchKinoPoiskHistoryHandler fetchKinoPoiskHistoryHandler;

    @KafkaHandler(isDefault = true)
    public void handle(UserHistoryEvent event, @Header(KafkaHeaders.RECEIVED_KEY) UUID userId) {
        logger.info("Received event with key: {} and message: {}", userId, event.toString());

        fetchKinoPoiskHistoryHandler.sendHistory(userId, event);
    }
}
