package com.service.core.controllers;

import com.service.core.security.services.MovieRegressionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/predict")
public class MovieRegressionController {

    private final MovieRegressionService service;

    @Operation(summary = "Predict movie rating")
    @GetMapping("/rating")
    public String predict(@RequestParam String title) {
        Double prediction = service.predictRating(title);
        return prediction != null ?
                String.format(prediction.toString()) :
                "-";
    }
}
