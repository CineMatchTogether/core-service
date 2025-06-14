package com.service.core.services;

import com.service.core.models.entities.Setting;
import com.service.core.repositories.SettingsRepository;
import com.service.core.services.exceptions.SettingsNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SettingServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private SettingService settingService;

    private Setting setting;

    @BeforeEach
    void setUp() {
        setting = new Setting(1L, "search-token-123", "cookie-value");
    }

    @Test
    void create_Success() {
        // Arrange
        when(settingsRepository.save(any(Setting.class))).thenReturn(setting);

        // Act
        Setting result = settingService.create(setting);

        // Assert
        assertNotNull(result);
        assertEquals(setting.getId(), result.getId());
        assertEquals(setting.getSearchToken(), result.getSearchToken());
        assertEquals(setting.getCookie(), result.getCookie());
        verify(settingsRepository).save(setting);
    }

    @Test
    void getSetting_Success() {
        // Arrange
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(setting));

        // Act
        Setting result = settingService.getSetting();

        // Assert
        assertNotNull(result);
        assertEquals(setting.getId(), result.getId());
        assertEquals(setting.getSearchToken(), result.getSearchToken());
        assertEquals(setting.getCookie(), result.getCookie());
        verify(settingsRepository).findById(1L);
    }

    @Test
    void getSetting_NotFound() {
        // Arrange
        when(settingsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SettingsNotFoundException.class, () -> settingService.getSetting());
        verify(settingsRepository).findById(1L);
    }
} 