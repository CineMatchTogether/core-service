package com.service.core.websockets.message;

import lombok.Builder;

@Builder
public record WebSocketMessage(MessageType messageType, MessageStatus messageStatus, String content) {
}
