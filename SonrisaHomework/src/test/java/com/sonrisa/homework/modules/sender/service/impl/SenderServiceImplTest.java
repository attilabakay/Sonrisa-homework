package com.sonrisa.homework.modules.sender.service.impl;

import com.sonrisa.homework.auth.RequestingUser;
import com.sonrisa.homework.common.enums.SenderType;
import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.sender.dto.request.SenderRequest;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// The ownership enforcement here is the fix for a real gap: before it existed, any
// authenticated user could read or write another user's senders just by knowing their UUID.
@ExtendWith(MockitoExtension.class)
class SenderServiceImplTest {

    @Mock
    private SenderRepository senderRepository;
    @Mock
    private UserRepository userRepository;

    private SenderServiceImpl service;

    private UUID ownerId;
    private User owner;

    @BeforeEach
    void setUp() {
        service = new SenderServiceImpl(senderRepository, userRepository);
        ownerId = UUID.randomUUID();
        owner = User.builder().id(ownerId).email("owner@example.com").password("hash").admin(false).active(true).build();
    }

    private Sender sender(User user) {
        return Sender.builder().id(UUID.randomUUID()).type(SenderType.EMAIL).user(user).config("owner@example.com").build();
    }

    @Test
    void ownerCanCreateTheirOwnSender() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(senderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SenderRequest request = new SenderRequest(SenderType.EMAIL, ownerId, "owner@example.com");

        var result = service.create(request, new RequestingUser(ownerId, false));

        assertThat(result.userId()).isEqualTo(ownerId);
    }

    @Test
    void regularUserCannotCreateASenderForSomeoneElse() {
        SenderRequest request = new SenderRequest(SenderType.EMAIL, ownerId, "owner@example.com");
        RequestingUser someoneElse = new RequestingUser(UUID.randomUUID(), false);

        assertThatThrownBy(() -> service.create(request, someoneElse))
                .isInstanceOf(AccessDeniedException.class);
        verify(senderRepository, never()).save(any());
    }

    @Test
    void adminCanCreateASenderForAnyUser() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(senderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SenderRequest request = new SenderRequest(SenderType.EMAIL, ownerId, "owner@example.com");

        var result = service.create(request, new RequestingUser(UUID.randomUUID(), true));

        assertThat(result.userId()).isEqualTo(ownerId);
    }

    @Test
    void regularUserCannotListAnotherUsersSenders() {
        RequestingUser someoneElse = new RequestingUser(UUID.randomUUID(), false);

        assertThatThrownBy(() -> service.getByUser(ownerId, someoneElse))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ownerCanListTheirOwnSenders() {
        when(senderRepository.findByUserId(ownerId)).thenReturn(java.util.List.of(sender(owner)));

        var result = service.getByUser(ownerId, new RequestingUser(ownerId, false));

        assertThat(result).hasSize(1);
    }

    @Test
    void regularUserCannotFetchAnotherUsersSenderById() {
        Sender existing = sender(owner);
        when(senderRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        RequestingUser someoneElse = new RequestingUser(UUID.randomUUID(), false);

        assertThatThrownBy(() -> service.getById(existing.getId(), someoneElse))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void gettingAMissingSenderRaisesNotFoundBeforeAnyOwnershipCheck() {
        UUID missingId = UUID.randomUUID();
        when(senderRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(missingId, new RequestingUser(ownerId, false)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateRefusesToReassignASenderToAnotherUserWhenNotAdmin() {
        Sender existing = sender(owner);
        when(senderRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        UUID someoneElseId = UUID.randomUUID();
        SenderRequest request = new SenderRequest(SenderType.SLACK, someoneElseId, "https://hooks.slack.com/x");

        assertThatThrownBy(() -> service.update(existing.getId(), request, new RequestingUser(ownerId, false)))
                .isInstanceOf(AccessDeniedException.class);
        verify(senderRepository, never()).save(any());
    }

    @Test
    void ownerCanUpdateTheirOwnSenderWithoutReassigning() {
        Sender existing = sender(owner);
        when(senderRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(senderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SenderRequest request = new SenderRequest(SenderType.SLACK, ownerId, "https://hooks.slack.com/new");

        var result = service.update(existing.getId(), request, new RequestingUser(ownerId, false));

        assertThat(result.type()).isEqualTo(SenderType.SLACK);
        assertThat(result.config()).isEqualTo("https://hooks.slack.com/new");
    }

    @Test
    void regularUserCannotDeleteAnotherUsersSender() {
        Sender existing = sender(owner);
        when(senderRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        RequestingUser someoneElse = new RequestingUser(UUID.randomUUID(), false);

        assertThatThrownBy(() -> service.delete(existing.getId(), someoneElse))
                .isInstanceOf(AccessDeniedException.class);
        verify(senderRepository, never()).delete(any());
    }
}
