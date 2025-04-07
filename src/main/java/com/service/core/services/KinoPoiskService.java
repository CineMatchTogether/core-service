package com.service.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.core.models.entities.Setting;
import com.service.core.services.exceptions.KinoPoiskIdNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KinoPoiskService {

    private final RestTemplate restTemplate;
    private final SettingService settingService;

    public synchronized Long getKinoPoiskId(String login) throws Exception {
        Setting setting = settingService.getSetting();
        String token = setting.getSearchToken();
        String cookie = setting.getCookie();

        String url = "https://www.kinopoisk.ru/handler_search_login.php?token=" + token + "&q=" + login;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() != HttpStatusCode.valueOf(200)) return null;

        try {
            return extractKinoPoiskId(response.getBody());
        } catch (KinoPoiskIdNotFoundException e) {
            throw new KinoPoiskIdNotFoundException(login);
        }
    }

    private Long extractKinoPoiskId(String responseBody) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray() && root.isEmpty()) throw new KinoPoiskIdNotFoundException();

            if (root.isArray() && !root.isEmpty()) {
                return root.get(0).path("id").asLong(0);
            }
            return null;
        } catch (KinoPoiskIdNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to parse KinoPoisk ID", e);
        }
    }
}
