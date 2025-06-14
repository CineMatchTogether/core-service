package com.service.core.services;

import com.service.core.models.entities.User;
import com.service.core.models.entities.UserMovieHistory;
import com.service.core.models.entities.UserMovieHistoryPK;
import com.service.core.repositories.UserMovieHistoryRepository;
import com.service.core.security.services.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserMovieHistoryServiceTest {

    @Mock
    private UserMovieHistoryRepository userMovieHistoryRepository;

    @InjectMocks
    private UserMovieHistoryService userMovieHistoryService;

    @Captor
    private ArgumentCaptor<List<UserMovieHistory>> userMovieHistoryListCaptor;

    private User user;
    private List<Long> movieIds;
    private UserMovieHistory userMovieHistory;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        movieIds = List.of(1L, 2L, 3L);

        userMovieHistory = new UserMovieHistory(new UserMovieHistoryPK(user, 1L));
    }

    @Test
    void saveAll_Success() throws UserNotFoundException {
        // Arrange
        when(userMovieHistoryRepository.saveAll(any())).thenReturn(List.of(userMovieHistory));

        // Act
        userMovieHistoryService.saveAll(user, movieIds);

        // Assert
        verify(userMovieHistoryRepository).saveAll(userMovieHistoryListCaptor.capture());
        List<UserMovieHistory> capturedHistories = userMovieHistoryListCaptor.getValue();
        
        assertEquals(movieIds.size(), capturedHistories.size());
        for (int i = 0; i < movieIds.size(); i++) {
            assertEquals(user, capturedHistories.get(i).getUserMovieHistoryPK().getUser());
            assertEquals(movieIds.get(i), capturedHistories.get(i).getUserMovieHistoryPK().getMovieId());
        }
    }

    @Test
    void saveAll_EmptyMovieIds() throws UserNotFoundException {
        // Arrange
        List<Long> emptyMovieIds = List.of();

        // Act
        userMovieHistoryService.saveAll(user, emptyMovieIds);

        // Assert
        verify(userMovieHistoryRepository).saveAll(userMovieHistoryListCaptor.capture());
        assertTrue(userMovieHistoryListCaptor.getValue().isEmpty());
    }

    @Test
    void save_Success() {
        // Arrange
        Long movieId = 1L;
        when(userMovieHistoryRepository.save(any(UserMovieHistory.class))).thenReturn(userMovieHistory);

        // Act
        Long result = userMovieHistoryService.save(user, movieId);

        // Assert
        assertEquals(movieId, result);
        verify(userMovieHistoryRepository).save(any(UserMovieHistory.class));
    }
} 