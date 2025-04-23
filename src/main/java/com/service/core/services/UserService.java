package com.service.core.services;

import com.service.core.models.entities.User;
import com.service.core.models.entities.YandexAccount;
import com.service.core.models.entities.enums.ERole;
import com.service.core.models.entities.enums.EStatusFetching;
import com.service.core.repositories.RoleRepository;
import com.service.core.repositories.UserRepository;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.exceptions.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KinoPoiskService kinoPoiskService;
    private final UserMovieHistoryService userMovieHistoryService;

    public User getOne(UUID userId) throws UserNotFoundException {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User createUser(User user) throws UserAlreadyExistException {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException("Username is already taken!");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException("Email is already taken!");
        }

        return userRepository.save(user);
    }

    public synchronized User creatOrGetOauthUser(String login, String email, DefaultOAuth2User principal) throws RoleNotFoundException, UserNotFoundException {
        boolean userExist = userIsExist(login);

        if (userExist) {
            return userRepository.findByUsername(login).orElseThrow(() -> new UserNotFoundException(login, email));
        }

        User user = User.builder()
                .username(login)
                .email(email)
                .roles(Set.of(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(RoleNotFoundException::new)))
                .build();


        YandexAccount yandexAccount = YandexAccount.builder()
                .yandexId(Long.valueOf(principal.getAttribute("id")))
                .realName(principal.getAttribute("real_name"))
                .login(principal.getAttribute("login"))
                .email(principal.getAttribute("default_email"))
                .statusFetching(EStatusFetching.NOT_ATTEMPTED)
                .user(user)
                .build();

        user.setYandexAccount(yandexAccount);

        return userRepository.save(user);
    }

    public void fetchKinoPoiskId(UUID userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Long kinoPioskId = kinoPoiskService.getKinoPoiskId(user.getYandexAccount().getLogin());
        user.getYandexAccount().setKinopoiskId(kinoPioskId);
        userRepository.save(user);
    }

    public boolean userIsExist(String login) {
        return userRepository.existsByUsername(login);
    }

    public List<Long> getWatchHistoryByUserId(UUID userId) throws UserNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId))
                .getMovieHistory().stream()
                .map(h -> h.getUserMovieHistoryPK().getMovieId())
                .toList();
    }

    public Long addWatchedMovie(UUID id, Long movieId) throws UserNotFoundException {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return userMovieHistoryService.save(user, movieId);
    }
}
