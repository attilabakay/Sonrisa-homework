package com.sonrisa.homework.modules.alert.service.impl;

import com.sonrisa.homework.auth.RequestingUser;
import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.common.enums.SenderType;
import com.sonrisa.homework.modules.alert.dto.request.AlertRequest;
import com.sonrisa.homework.modules.alert.model.Alert;
import com.sonrisa.homework.modules.alert.repository.AlertRepository;
import com.sonrisa.homework.modules.sender.model.Sender;
import com.sonrisa.homework.modules.sender.repository.SenderRepository;
import com.sonrisa.homework.modules.user.model.User;
import com.sonrisa.homework.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Same ownership fix as SenderServiceImplTest, applied to Alert -- workflow.md documents Alert
// reads as "scoped to the logged-in user", which nothing enforced server-side before this.
@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private AlertRepository alertRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SenderRepository senderRepository;

    private AlertServiceImpl service;

    private UUID ownerId;
    private User owner;
    private Sender ownerSender;

    @BeforeEach
    void setUp() {
        service = new AlertServiceImpl(alertRepository, userRepository, senderRepository);
        ownerId = UUID.randomUUID();
        owner = User.builder().id(ownerId).email("owner@example.com").password("hash").admin(false).active(true).build();
        ownerSender = Sender.builder().id(UUID.randomUUID()).type(SenderType.EMAIL).user(owner).config("owner@example.com").build();
    }

    private Alert alert(User user) {
        return Alert.builder().id(UUID.randomUUID()).type(DataType.NEWS).criteria("{\"keyword\":\"x\"}")
                .user(user).sender(ownerSender).active(true).build();
    }

    @Test
    void ownerCanCreateTheirOwnAlert() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(senderRepository.findById(ownerSender.getId())).thenReturn(Optional.of(ownerSender));
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        AlertRequest request = new AlertRequest(DataType.NEWS, "{\"keyword\":\"x\"}", ownerId, ownerSender.getId());

        var result = service.create(request, new RequestingUser(ownerId, false));

        assertThat(result.userId()).isEqualTo(ownerId);
    }

    @Test
    void regularUserCannotCreateAnAlertForSomeoneElse() {
        AlertRequest request = new AlertRequest(DataType.NEWS, "{\"keyword\":\"x\"}", ownerId, ownerSender.getId());
        RequestingUser someoneElse = new RequestingUser(UUID.randomUUID(), false);

        assertThatThrownBy(() -> service.create(request, someoneElse)).isInstanceOf(AccessDeniedException.class);
        verify(alertRepository, never()).save(any());
    }

    @Test
    void regularUserCannotViewAnotherUsersActiveAlerts() {
        RequestingUser someoneElse = new RequestingUser(UUID.randomUUID(), false);

        assertThatThrownBy(() -> service.getActiveByUser(ownerId, someoneElse)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanViewAnyUsersActiveAlerts() {
        when(alertRepository.findByUserIdAndActiveTrue(ownerId)).thenReturn(List.of(alert(owner)));

        var result = service.getActiveByUser(ownerId, new RequestingUser(UUID.randomUUID(), true));

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllByUserHasNoOwnershipCheckSinceItsAlreadyRoleGatedAtTheSecurityLayer() {
        when(alertRepository.findByUserId(ownerId)).thenReturn(List.of(alert(owner)));

        var result = service.getAllByUser(ownerId);

        assertThat(result).hasSize(1);
    }

    @Test
    void regularUserCannotDeactivateAnotherUsersAlert() {
        Alert existing = alert(owner);
        when(alertRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        RequestingUser someoneElse = new RequestingUser(UUID.randomUUID(), false);

        assertThatThrownBy(() -> service.deactivate(existing.getId(), someoneElse)).isInstanceOf(AccessDeniedException.class);
        verify(alertRepository, never()).save(any());
    }

    @Test
    void ownerCanDeactivateTheirOwnAlert() {
        Alert existing = alert(owner);
        when(alertRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.deactivate(existing.getId(), new RequestingUser(ownerId, false));

        assertThat(result.active()).isFalse();
    }

    @Test
    void regularUserCannotDeleteAnotherUsersAlert() {
        Alert existing = alert(owner);
        when(alertRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        RequestingUser someoneElse = new RequestingUser(UUID.randomUUID(), false);

        assertThatThrownBy(() -> service.delete(existing.getId(), someoneElse)).isInstanceOf(AccessDeniedException.class);
        verify(alertRepository, never()).delete(any());
    }
}
