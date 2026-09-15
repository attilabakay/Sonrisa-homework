package com.sonrisa.homework.modules.user.service.impl;

import com.sonrisa.homework.common.exception.ConflictException;
import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.alert.repository.AlertRepository;
import com.sonrisa.homework.modules.user.dto.request.UserRegisterRequest;
import com.sonrisa.homework.modules.user.model.User;
import com.sonrisa.homework.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, alertRepository, passwordEncoder);
    }

    @Test
    void registerRejectsAnAlreadyRegisteredEmail() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
        UserRegisterRequest request = new UserRegisterRequest("taken@example.com", "password123");

        assertThatThrownBy(() -> service.register(request)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerHashesThePasswordAndDefaultsToActiveNonAdmin() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-value");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UserRegisterRequest request = new UserRegisterRequest("new@example.com", "password123");

        var result = service.register(request);

        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.admin()).isFalse();
        assertThat(result.active()).isTrue();
        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed-value");
    }

    @Test
    void deactivateFlipsActiveToFalse() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).email("a@example.com").password("hash").admin(false).active(true).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.deactivate(id);

        assertThat(result.active()).isFalse();
    }

    @Test
    void deactivatingAMissingUserRaisesNotFound() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(missingId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllJoinsEachUserToTheirOwnAlertsOnly() {
        User alice = User.builder().id(UUID.randomUUID()).email("alice@example.com").password("h").admin(false).active(true).build();
        User bob = User.builder().id(UUID.randomUUID()).email("bob@example.com").password("h").admin(false).active(true).build();
        when(userRepository.findAll()).thenReturn(List.of(alice, bob));
        when(alertRepository.findByUserId(alice.getId())).thenReturn(List.of());
        when(alertRepository.findByUserId(bob.getId())).thenReturn(List.of());

        var result = service.getAll();

        assertThat(result).extracting("email").containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
        verify(alertRepository).findByUserId(alice.getId());
        verify(alertRepository).findByUserId(bob.getId());
    }
}
