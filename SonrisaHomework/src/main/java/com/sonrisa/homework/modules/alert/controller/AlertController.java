package com.sonrisa.homework.modules.alert.controller;

import com.sonrisa.homework.auth.RequestingUser;
import com.sonrisa.homework.modules.alert.dto.base.AlertDTO;
import com.sonrisa.homework.modules.alert.dto.request.AlertRequest;
import com.sonrisa.homework.modules.alert.service.base.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertDTO create(@Valid @RequestBody AlertRequest request, Authentication authentication) {
        return alertService.create(request, RequestingUser.from(authentication));
    }

    @GetMapping("/user/{userId}")
    public List<AlertDTO> getActiveByUser(@PathVariable UUID userId, Authentication authentication) {
        return alertService.getActiveByUser(userId, RequestingUser.from(authentication));
    }

    // Admin view: all of a user's alerts, active or not (mvp final.md §5). Already
    // ROLE_ADMIN-gated in SecurityConfig, so no requester check needed here.
    @GetMapping("/user/{userId}/all")
    public List<AlertDTO> getAllByUser(@PathVariable UUID userId) {
        return alertService.getAllByUser(userId);
    }

    @GetMapping("/{id}")
    public AlertDTO getById(@PathVariable UUID id, Authentication authentication) {
        return alertService.getById(id, RequestingUser.from(authentication));
    }

    // The MVP's "delete" op — flips isActive, preserves NotificationAttempt FK history.
    @PatchMapping("/{id}/deactivate")
    public AlertDTO deactivate(@PathVariable UUID id, Authentication authentication) {
        return alertService.deactivate(id, RequestingUser.from(authentication));
    }

    // Hard delete: possible but not preferred, since it discards audit history (workflow.md §4).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication authentication) {
        alertService.delete(id, RequestingUser.from(authentication));
    }
}
