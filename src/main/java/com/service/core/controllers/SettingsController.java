package com.service.core.controllers;

import com.service.core.mappers.SettingMapper;
import com.service.core.models.dto.SettingDto;
import com.service.core.services.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingMapper settingMapper;
    private final SettingService settingService;

    @Operation(summary = "Set settings")
    @PostMapping("/set-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingDto> setSettings(@RequestBody SettingDto dto) {
        return ResponseEntity.ok().body(settingMapper.toDto(settingService.create(settingMapper.toModel(dto))));
    }
}
