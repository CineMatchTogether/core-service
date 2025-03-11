package com.service.core.services;

import com.service.core.models.entities.Setting;
import com.service.core.repositories.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingsRepository settingsRepository;
    public Setting create(Setting setting) {
        return settingsRepository.save(setting);
    }

    public Setting getSetting() {
        return settingsRepository.findById(1L).orElseThrow();
    }

}
