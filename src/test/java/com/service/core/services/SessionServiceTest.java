package com.service.core.services;

import com.service.core.models.entities.Session;
import com.service.core.models.entities.User;
import com.service.core.models.entities.UserSession;
import com.service.core.models.entities.UserSessionPK;
import com.service.core.models.entities.enums.ESessionStatus;
import com.service.core.repositories.SessionRepository;
import com.service.core.repositories.UserRepository;
import com.service.core.repositories.UserSessionRepository;
import com.service.core.services.exceptions.SessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private SessionService sessionService;

    private Session session;
    private User user;
    private UserSession userSession;
    private List<Long> movieIds;

    @BeforeEach
    void setUp() {
        String sessionId = "SESSION123";
        UUID userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        session = new Session();
        session.setId(sessionId);
        session.setSessionStatus(ESessionStatus.NEW);

        userSession = new UserSession();
        userSession.setUserSessionPK(new UserSessionPK(user, session));

        movieIds = List.of(1L, 2L, 3L);
    }

    @Test
    void startSession_Success() throws SessionNotFoundException {
        // Arrange
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(userSessionRepository.findBySessionId(session.getId())).thenReturn(List.of(userSession));
        when(recommendationService.getGroupRecommendations(any())).thenReturn(movieIds);
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        // Act
        Session result = sessionService.startSession(session.getId());

        // Assert
        assertNotNull(result);
        assertEquals(ESessionStatus.RUN, result.getSessionStatus());
        assertEquals(movieIds, result.getMoviesIds());
        verify(sessionRepository).findById(session.getId());
        verify(userSessionRepository).findBySessionId(session.getId());
        verify(recommendationService).getGroupRecommendations(any());
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void startSession_SessionNotFound() {
        // Arrange
        String nonExistentId = "NONEXISTENT";
        when(sessionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SessionNotFoundException.class, () -> sessionService.startSession(nonExistentId));
        verify(sessionRepository).findById(nonExistentId);
        verify(userSessionRepository, never()).findBySessionId(any());
        verify(recommendationService, never()).getGroupRecommendations(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void endSession_Success() {
        // Arrange
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        // Act
        sessionService.endSession(session.getId());

        // Assert
        assertEquals(ESessionStatus.ARCHIVE, session.getSessionStatus());
        verify(sessionRepository).findById(session.getId());
        verify(sessionRepository).save(session);
    }

    @Test
    void endSession_SessionNotFound() {
        // Arrange
        String nonExistentId = "NONEXISTENT";
        when(sessionRepository.findById(nonExistentId)).thenThrow(new RuntimeException());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> sessionService.endSession(nonExistentId));
        verify(sessionRepository).findById(nonExistentId);
        verify(sessionRepository, never()).save(any());
    }
} 