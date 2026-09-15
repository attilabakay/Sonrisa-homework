package com.sonrisa.homework.modules.sender.service.impl;

import com.sonrisa.homework.auth.RequestingUser;
import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.sender.dto.base.SenderDTO;
import com.sonrisa.homework.modules.sender.dto.request.SenderRequest;
import com.sonrisa.homework.modules.sender.model.Sender;
import com.sonrisa.homework.modules.sender.repository.SenderRepository;
import com.sonrisa.homework.modules.sender.service.base.SenderService;
import com.sonrisa.homework.modules.user.model.User;
import com.sonrisa.homework.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SenderServiceImpl implements SenderService {

    private final SenderRepository senderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SenderDTO create(SenderRequest request, RequestingUser requester) {
        if (!requester.canAccess(request.userId())) {
            throw new AccessDeniedException("Cannot create a sender for another user");
        }
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));
        Sender sender = Sender.builder()
                .type(request.type())
                .user(user)
                .config(request.config())
                .build();
        return SenderDTO.fromEntity(senderRepository.save(sender));
    }

    @Override
    public List<SenderDTO> getByUser(UUID userId, RequestingUser requester) {
        if (!requester.canAccess(userId)) {
            throw new AccessDeniedException("Cannot view another user's senders");
        }
        return senderRepository.findByUserId(userId).stream().map(SenderDTO::fromEntity).toList();
    }

    @Override
    public SenderDTO getById(UUID id, RequestingUser requester) {
        Sender sender = findEntity(id);
        requireAccess(requester, sender);
        return SenderDTO.fromEntity(sender);
    }

    @Override
    @Transactional
    public SenderDTO update(UUID id, SenderRequest request, RequestingUser requester) {
        Sender sender = findEntity(id);
        requireAccess(requester, sender);
        if (!requester.canAccess(request.userId())) {
            throw new AccessDeniedException("Cannot reassign a sender to another user");
        }
        if (!sender.getUser().getId().equals(request.userId())) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));
            sender.setUser(user);
        }
        sender.setType(request.type());
        sender.setConfig(request.config());
        return SenderDTO.fromEntity(senderRepository.save(sender));
    }

    @Override
    @Transactional
    public void delete(UUID id, RequestingUser requester) {
        Sender sender = findEntity(id);
        requireAccess(requester, sender);
        senderRepository.delete(sender);
    }

    private void requireAccess(RequestingUser requester, Sender sender) {
        if (!requester.canAccess(sender.getUser().getId())) {
            throw new AccessDeniedException("Cannot access another user's sender");
        }
    }

    private Sender findEntity(UUID id) {
        return senderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found: " + id));
    }
}
