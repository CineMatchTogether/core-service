package com.service.core.websockets.message;

public record FilmDecisionContent(String sessionId, Long movieId, Boolean answer) {
}
