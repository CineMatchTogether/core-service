package com.service.core.services;

import com.service.core.models.entities.FilmDecision;
import com.service.core.models.entities.Session;
import com.service.core.models.entities.UserSession;
import com.service.core.models.entities.UserSessionPK;
import com.service.core.models.entities.enums.ESessionStatus;
import com.service.core.repositories.FilmDecisionRepository;
import com.service.core.repositories.SessionRepository;
import com.service.core.repositories.UserRepository;
import com.service.core.repositories.UserSessionRepository;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.exceptions.CannotConnectToSessionException;
import com.service.core.services.exceptions.SessionNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final FilmDecisionRepository filmDecisionRepository;
    private final RecommendationService recommendationService;

    public Session getSessionById(String id) throws SessionNotFoundException {
        return sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
    }

    public Session create() {
        var session = Session.builder()
                .sessionStatus(ESessionStatus.NEW)
                .build();
        return sessionRepository.save(session);
    }

    public Session connectToSession(UUID userId, String sessionId) throws SessionNotFoundException, CannotConnectToSessionException, UserNotFoundException {
        var session = sessionRepository.findById(sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        UserSessionPK userSessionPK = new UserSessionPK(user, session);
        if (userSessionRepository.findById(userSessionPK).isPresent())
            return userSessionRepository.findById(userSessionPK).get().getUserSessionPK().getSession();

        if (session.getSessionStatus() != ESessionStatus.NEW) throw new CannotConnectToSessionException(sessionId);
        UserSession userSession = new UserSession(new UserSessionPK(user, session));

        return userSessionRepository.save(userSession).getUserSessionPK().getSession();
    }

    @SneakyThrows
    @Transactional
    public Optional<Long> saveDecision(UUID userId, String sessionId, Boolean answer, Long movieId) {
        var session = sessionRepository.findById(sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        UserSession userSession = userSessionRepository.findById(new UserSessionPK(user, session)).orElseThrow();

        var filmDecision = FilmDecision.builder()
                .movieId(movieId)
                .answer(answer)
                .userSession(userSession)
                .build();

        if (filmDecisionRepository.existsByUserSessionAndMovieId(userSession, movieId)) return Optional.empty();

        filmDecisionRepository.save(filmDecision);

        int countUsers = userSessionRepository.findBySessionId(sessionId).size();

        List<FilmDecision> decisions = filmDecisionRepository.findBySessionId(sessionId);
        return decisions.stream()
                .filter(FilmDecision::isAnswer)
                .collect(Collectors.groupingBy(FilmDecision::getMovieId, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() == countUsers)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    @Transactional
    public List<UUID> getSessionUsers(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId).stream()
                .map(us -> us.getUserSessionPK().getUser().getId())
                .toList();
    }

    @Transactional
    public Session startSession(String id) throws SessionNotFoundException {
        var session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
        var ids = recommendationService.getGroupRecommendations(userSessionRepository.
                findBySessionId(session.getId()).stream()
                .map(us -> us.getUserSessionPK().getUser().getId())
                .toList());
        session.setSessionStatus(ESessionStatus.RUN);
        session.setMoviesIds(ids);

        return sessionRepository.save(session);
    }

    public void endSession(String id) {
        var session = sessionRepository.findById(id).orElseThrow();
        session.setSessionStatus(ESessionStatus.ARCHIVE);

        sessionRepository.save(session);
    }
}
