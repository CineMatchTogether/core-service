package com.service.core.controllers;

import com.service.core.security.services.UserDetailsImpl;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDetailsImpl userDetails;
    private UUID userId;
    private List<Long> watchHistory;
    private Long movieId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userDetails = new UserDetailsImpl(userId, "testuser", "test@example.com", "password", null);
        watchHistory = Arrays.asList(1L, 2L, 3L);
        movieId = 4L;
    }

    @Test
    void getWatchHistoryByUserId_Success() throws UserNotFoundException {
        // Arrange
        when(userService.getWatchHistoryByUserId(userId)).thenReturn(watchHistory);

        // Act
        ResponseEntity<List<Long>> response = userController.getWatchHistoryByUserId(userId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(watchHistory, response.getBody());
        verify(userService).getWatchHistoryByUserId(userId);
    }

    @Test
    void getWatchHistoryByUserId_UserNotFound() throws UserNotFoundException {
        // Arrange
        when(userService.getWatchHistoryByUserId(userId)).thenThrow(new UserNotFoundException(userId));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userController.getWatchHistoryByUserId(userId));
        verify(userService).getWatchHistoryByUserId(userId);
    }

    @Test
    void getWatchHistory_Success() throws UserNotFoundException {
        // Arrange
        when(userService.getWatchHistoryByUserId(userId)).thenReturn(watchHistory);

        // Act
        ResponseEntity<List<Long>> response = userController.getWatchHistory(userDetails);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(watchHistory, response.getBody());
        verify(userService).getWatchHistoryByUserId(userId);
    }

    @Test
    void getWatchHistory_UserNotFound() throws UserNotFoundException {
        // Arrange
        when(userService.getWatchHistoryByUserId(userId)).thenThrow(new UserNotFoundException(userId));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userController.getWatchHistory(userDetails));
        verify(userService).getWatchHistoryByUserId(userId);
    }

    @Test
    void addWatchedMovie_Success() throws UserNotFoundException {
        // Arrange
        when(userService.addWatchedMovie(userId, movieId)).thenReturn(movieId);

        // Act
        ResponseEntity<Long> response = userController.addWatchedMovie(userDetails, movieId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(movieId, response.getBody());
        verify(userService).addWatchedMovie(userId, movieId);
    }

    @Test
    void addWatchedMovie_UserNotFound() throws UserNotFoundException {
        // Arrange
        when(userService.addWatchedMovie(userId, movieId)).thenThrow(new UserNotFoundException(userId));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userController.addWatchedMovie(userDetails, movieId));
        verify(userService).addWatchedMovie(userId, movieId);
    }
}
