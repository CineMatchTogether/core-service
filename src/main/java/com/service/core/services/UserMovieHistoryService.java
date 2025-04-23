package com.service.core.services;

import com.service.core.models.entities.User;
import com.service.core.models.entities.UserMovieHistory;
import com.service.core.models.entities.UserMovieHistoryPK;
import com.service.core.repositories.UserMovieHistoryRepository;
import com.service.core.security.services.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMovieHistoryService {

    private final UserMovieHistoryRepository userMovieHistoryRepository;

    public void saveAll(User user, List<Long> ids) throws UserNotFoundException {
        userMovieHistoryRepository.saveAll(Arrays.asList(ids.stream()
                .map(i -> new UserMovieHistory(new UserMovieHistoryPK(user, i)))
                .toArray(UserMovieHistory[]::new)));
    }

    public Long save(User user, Long id) {
        return userMovieHistoryRepository.save(new UserMovieHistory(new UserMovieHistoryPK(user, id))).getUserMovieHistoryPK().getMovieId();
    }
}
