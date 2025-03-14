package com.service.core.services;

import com.service.core.models.entities.User;
import com.service.core.models.entities.YandexAccount;
import com.service.core.models.entities.enums.ERole;
import com.service.core.repositories.RoleRepository;
import com.service.core.repositories.UserRepository;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.exceptions.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KinoPoiskService kinoPoiskService;

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    public User createUser(User user) throws UserAlreadyExistException {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException("Username is already taken!");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException("Email is already taken!");
        }

        return userRepository.save(user);
    }

    public User creatOrGetOauthUser(String login, String email, DefaultOAuth2User principal) throws RoleNotFoundException, UserNotFoundException {
        boolean userExist = userIsExist(login, email);

        if (userExist) {
            return userRepository.findUserByUsernameOrEmail(login, email).orElseThrow(() -> new UserNotFoundException(login, email));
        }

        User user = User.builder()
                .username(login)
                .email(email)
                .roles(Set.of(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(RoleNotFoundException::new)))
                .build();

        try {
            YandexAccount yandexAccount = YandexAccount.builder()
                    .yandexId(Long.valueOf(principal.getAttribute("id")))
                    .realName(principal.getAttribute("real_name"))
                    .login(principal.getAttribute("login"))
                    .email(principal.getAttribute("default_email"))
                    .kinopoiskId(kinoPoiskService.getKinoPoiskId(principal.getAttribute("login")))
                    .user(user)
                    .build();

            user.setYandexAccount(yandexAccount);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        return userRepository.save(user);
    }

    private boolean userIsExist(String login, String email) {
        return userRepository.existsByUsername(login) || userRepository.existsByEmail(email);
    }
}
