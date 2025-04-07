package com.service.core.controllers;

import com.service.core.security.services.UserDetailsImpl;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get user watch history by userId")
    @GetMapping("/{userId}/watch-history")
    public ResponseEntity<List<Long>> getWatchHistoryByUserId(@PathVariable UUID userId) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getWatchHistoryByUserId(userId));
    }

    @Operation(summary = "Get user watch history")
    @GetMapping("/watch-history")
    public ResponseEntity<List<Long>> getWatchHistory(@AuthenticationPrincipal UserDetailsImpl userDetails) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getWatchHistoryByUserId(userDetails.getId()));
    }

}
