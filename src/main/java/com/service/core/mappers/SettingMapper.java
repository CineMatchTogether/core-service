package com.service.core.mappers;

import com.service.core.models.dto.SettingDto;
import com.service.core.models.entities.Setting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SettingMapper {
    SettingDto toDto(Setting setting);

    @Mapping(target = "id", ignore = true)
    Setting toModel(SettingDto dto);
}
