package com.sonrisa.homework.modules.notification.controller;

import com.sonrisa.homework.modules.notification.dto.base.NotificationAttemptDTO;
import com.sonrisa.homework.modules.notification.dto.request.NotificationAttemptRequest;
import com.sonrisa.homework.modules.notification.service.base.NotificationAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notification-attempts")
@RequiredArgsConstructor
public class NotificationAttemptController {

    private final NotificationAttemptService notificationAttemptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationAttemptDTO create(@Valid @RequestBody NotificationAttemptRequest request) {
        return notificationAttemptService.create(request);
    }

    // Admin "get all" view, the debugging surface for "did this fire, did it succeed" (workflow.md §5).
    @GetMapping
    public List<NotificationAttemptDTO> getAll() {
        return notificationAttemptService.getAll();
    }

    @GetMapping("/alert/{alertId}")
    public List<NotificationAttemptDTO> getByAlert(@PathVariable UUID alertId) {
        return notificationAttemptService.getByAlert(alertId);
    }
}
