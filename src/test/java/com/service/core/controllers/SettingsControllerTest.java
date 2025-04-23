package com.service.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.core.mappers.SettingMapper;
import com.service.core.models.dto.SettingDto;
import com.service.core.models.entities.Setting;
import com.service.core.services.SettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SettingsControllerTest {

    @Mock
    private SettingMapper settingMapper;

    @Mock
    private SettingService settingService;

    @InjectMocks
    private SettingsController settingsController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SettingDto settingDto;
    private Setting setting;

    @BeforeEach
    void setUp() {
        // Initialize test data
        settingDto = new SettingDto("search-token-123", "cookie-value");
        setting = new Setting(1L, "search-token-123", "cookie-value");

        // Initialize MockMvc and ObjectMapper
        mockMvc = MockMvcBuilders.standaloneSetup(settingsController).build();
        objectMapper = new ObjectMapper();
    }

    // Unit Tests
    @Test
    void setSettings_Success() {
        // Arrange
        when(settingMapper.toModel(settingDto)).thenReturn(setting);
        when(settingService.create(setting)).thenReturn(setting);
        when(settingMapper.toDto(setting)).thenReturn(settingDto);

        // Act
        ResponseEntity<SettingDto> response = settingsController.setSettings(settingDto);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(settingDto, response.getBody());
        verify(settingMapper).toModel(settingDto);
        verify(settingService).create(setting);
        verify(settingMapper).toDto(setting);
    }

    @Test
    void setSettings_InvalidInput_ThrowsException() {
        // Arrange
        when(settingMapper.toModel(settingDto)).thenReturn(setting);
        when(settingService.create(setting)).thenThrow(new IllegalArgumentException("Invalid setting"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> settingsController.setSettings(settingDto));
        verify(settingMapper).toModel(settingDto);
        verify(settingService).create(setting);
        verify(settingMapper, never()).toDto(any());
    }

    // Integration Tests for Security
    @Test
    @WithMockUser(roles = "ADMIN")
    void setSettings_WithAdminRole_Success() throws Exception {
        // Arrange
        when(settingMapper.toModel(settingDto)).thenReturn(setting);
        when(settingService.create(setting)).thenReturn(setting);
        when(settingMapper.toDto(setting)).thenReturn(settingDto);

        // Act & Assert
        mockMvc.perform(post("/api/set-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settingDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(settingMapper).toModel(settingDto);
        verify(settingService).create(setting);
        verify(settingMapper).toDto(setting);
    }

}
