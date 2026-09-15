package com.sonrisa.homework.modules.user.service.impl;

import com.sonrisa.homework.common.exception.ConflictException;
import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.user.dto.base.UserDTO;
import com.sonrisa.homework.modules.user.dto.request.UserRegisterRequest;
import com.sonrisa.homework.modules.user.model.User;
import com.sonrisa.homework.modules.user.repository.UserRepository;
import com.sonrisa.homework.modules.user.service.base.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .admin(false)
                .active(true)
                .build();
        return UserDTO.fromEntity(userRepository.save(user));
    }

    @Override
    public List<UserDTO> getAll() {
        return userRepository.findAll().stream().map(UserDTO::fromEntity).toList();
    }

    @Override
    public UserDTO getById(UUID id) {
        return UserDTO.fromEntity(findEntity(id));
    }

    @Override
    public UserDTO getByEmail(String email) {
        return UserDTO.fromEntity(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));
    }

    @Override
    @Transactional
    public UserDTO deactivate(UUID id) {
        User user = findEntity(id);
        user.setActive(false);
        return UserDTO.fromEntity(userRepository.save(user));
    }

    private User findEntity(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
