package com.service.core.websockets.message;

import lombok.NoArgsConstructor;

public record FilmDecisionContent(String sessionId, Long movieId, Boolean answer) {
}
