package com.service.core.websockets.repositories;


import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class SessionRepositoryImpl implements SessionRepository<UUID, WebSocketSession> {

    private final Map<UUID, WebSocketSession> sessions = new HashMap<>();
    @Override
    public void put(UUID key, WebSocketSession value) {
        sessions.put(key, value);
    }

    @Override
    public void remove(UUID key) {
        sessions.remove(key);
    }

    @Override
    public WebSocketSession get(UUID key) {
        return sessions.get(key);
    }
}
