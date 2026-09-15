package com.sonrisa.homework.modules.user.controller;

import com.sonrisa.homework.modules.user.dto.base.UserDTO;
import com.sonrisa.homework.modules.user.dto.request.UserRegisterRequest;
import com.sonrisa.homework.modules.user.service.base.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO register(@Valid @RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    // Admin "get all users" view (mvp final.md §5).
    @GetMapping
    public List<UserDTO> getAll() {
        return userService.getAll();
    }

    // "Who am I" for the HTTP Basic principal — there's no login endpoint, so the client
    // attaches credentials up front and calls this to confirm them and learn its own id.
    @GetMapping("/me")
    public UserDTO me(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    // No self-serve update/delete per workflow.md — admin can only enable/disable.
    @PatchMapping("/{id}/deactivate")
    public UserDTO deactivate(@PathVariable UUID id) {
        return userService.deactivate(id);
    }
}
