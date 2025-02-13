package com.service.core.services;

import com.service.core.models.entities.User;
import com.service.core.models.entities.enums.ERole;
import com.service.core.repositories.RoleRepository;
import com.service.core.repositories.UserRepository;
import com.service.core.services.exceptions.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) throws UserAlreadyExistException {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException("Username is already taken!");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException("Email is already taken!");
        }

        return userRepository.save(user);
    }

}
