package com.service.core.services;

import com.service.core.models.entities.User;
import com.service.core.models.entities.enums.ERole;
import com.service.core.repositories.RoleRepository;
import com.service.core.repositories.UserRepository;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.exceptions.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User createUser(User user) throws UserAlreadyExistException {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException("Username is already taken!");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException("Email is already taken!");
        }

        return userRepository.save(user);
    }

    public User creatOrGetOauthUser(String login, String email) throws RoleNotFoundException, UserNotFoundException {
        return !userIsExist(login, email) ?
                userRepository.save(User.builder()
                        .username(login)
                        .email(email)
                        .roles(Set.of(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(RoleNotFoundException::new)))
                        .build()) :
                userRepository.findUserByUsernameOrEmail(login, email).orElseThrow(() -> new UserNotFoundException(login, email));
    }

    private boolean userIsExist(String login, String email) {
        return userRepository.existsByUsername(login) || userRepository.existsByEmail(email);
    }
}
