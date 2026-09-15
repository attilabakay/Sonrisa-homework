package com.sonrisa.homework.modules.sender.controller;

import com.sonrisa.homework.auth.RequestingUser;
import com.sonrisa.homework.modules.sender.dto.base.SenderDTO;
import com.sonrisa.homework.modules.sender.dto.request.SenderRequest;
import com.sonrisa.homework.modules.sender.service.base.SenderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/senders")
@RequiredArgsConstructor
public class SenderController {

    private final SenderService senderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SenderDTO create(@Valid @RequestBody SenderRequest request, Authentication authentication) {
        return senderService.create(request, RequestingUser.from(authentication));
    }

    @GetMapping("/user/{userId}")
    public List<SenderDTO> getByUser(@PathVariable UUID userId, Authentication authentication) {
        return senderService.getByUser(userId, RequestingUser.from(authentication));
    }

    @GetMapping("/{id}")
    public SenderDTO getById(@PathVariable UUID id, Authentication authentication) {
        return senderService.getById(id, RequestingUser.from(authentication));
    }

    @PutMapping("/{id}")
    public SenderDTO update(@PathVariable UUID id, @Valid @RequestBody SenderRequest request, Authentication authentication) {
        return senderService.update(id, request, RequestingUser.from(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication authentication) {
        senderService.delete(id, RequestingUser.from(authentication));
    }
}
