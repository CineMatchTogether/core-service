package com.service.core.services;

import com.service.core.models.entities.User;
import com.service.core.repositories.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value ("${property.app.recUrl}")
    private String recUrl;

    private final static String API_URL = "/recommend/group";

    public List<Long> getGroupRecommendations(List<UUID> userIds) {
        // Извлекаем пользователей и их историю просмотров
        List<User> users = userRepository.findAllById(userIds);

        // Формируем список историй просмотров для каждого пользователя
        List<List<Long>> watchedMovies = users.stream()
                .map(user -> user.getMovieHistory().stream()
                        .map(history -> history.getUserMovieHistoryPK().getMovieId()) // Предполагается, что Movie имеет id
                        .collect(Collectors.toList()))
                .filter(history -> !history.isEmpty())
                .collect(Collectors.toList());

        // Если нет истории просмотров, возвращаем пустой список
        if (watchedMovies.isEmpty()) {
            return Collections.emptyList();
        }

        // Формируем тело запроса
        RecommendationRequest request = new RecommendationRequest();
        request.setTop_n(20); // Можно настроить по необходимости
        request.setWatched_movies(watchedMovies);
        request.setWeights(new Weights(0.6, 0.2, 0.2));

        // Отправляем запрос к API
        ResponseEntity<RecommendationApiResponse> response = restTemplate.postForEntity(
                recUrl + API_URL,
                request,
                RecommendationApiResponse.class
        );

        // Проверяем ответ и возвращаем рекомендации
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getRecommendations().stream().map(RecommendationResponse::getMovieId).toList();
        }

        return Collections.emptyList();
    }
}

// DTO для запроса
@Data
class RecommendationRequest {
    private int top_n;
    private List<List<Long>> watched_movies;
    private Weights weights;
}

@Data
class Weights {
    private double content_based;
    private double item_based;
    private double user_based;

    public Weights(double contentBased, double itemBased, double userBased) {
        this.content_based = contentBased;
        this.item_based = itemBased;
        this.user_based = userBased;
    }
}

// DTO для ответа
@Data
class RecommendationApiResponse {
    private int count;
    private List<RecommendationResponse> recommendations;
}

@Data
class RecommendationResponse {
    private Long movieId;
    private String title;
}
