package com.service.core.mappers;

import com.service.core.models.dto.UserDto;
import com.service.core.models.entities.Role;
import com.service.core.models.entities.User;
import com.service.core.security.services.UserDetailsImpl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", source = "userDetails.authorities", qualifiedByName = "authoritiesToRoles")
    UserDto toDto(UserDetailsImpl userDetails);

    @Named("authoritiesToRoles")
    default Set<String> authoritiesToRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoles")
    UserDto toDto(User user);

    @Named("rolesToRoles")
    default Set<String> rolesToRoles(Set<Role> roles) {
        return roles.stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
    }
}
