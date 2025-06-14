package com.service.core.controllers;

import com.service.core.models.dto.SessionDto;
import com.service.core.models.entities.Session;
import com.service.core.security.services.UserDetailsImpl;
import com.service.core.security.services.exception.UserNotFoundException;
import com.service.core.services.SessionService;
import com.service.core.services.exceptions.CannotConnectToSessionException;
import com.service.core.services.exceptions.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<Session> create() {
        return ResponseEntity.ok(sessionService.create());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getOne(@PathVariable String id) throws SessionNotFoundException {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @PostMapping("/{id}/connect")
    public ResponseEntity<Session> connectToSession(@PathVariable String id, @AuthenticationPrincipal UserDetailsImpl userDetails) throws UserNotFoundException, CannotConnectToSessionException, SessionNotFoundException {
        return ResponseEntity.ok(sessionService.connectToSession(userDetails.getId(), id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Session> startSession(@PathVariable String id) throws SessionNotFoundException {
        return ResponseEntity.ok(sessionService.startSession(id));
    }
}
