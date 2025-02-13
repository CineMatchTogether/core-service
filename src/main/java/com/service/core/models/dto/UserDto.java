package com.service.core.models.dto;

import java.util.Set;
import java.util.UUID;

public record UserDto(UUID id, String username, String email, Set<String> roles) {
}
