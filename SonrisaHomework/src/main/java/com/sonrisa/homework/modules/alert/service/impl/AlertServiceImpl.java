package com.sonrisa.homework.modules.alert.service.impl;

import com.sonrisa.homework.auth.RequestingUser;
import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.alert.dto.base.AlertDTO;
import com.sonrisa.homework.modules.alert.dto.request.AlertRequest;
import com.sonrisa.homework.modules.alert.model.Alert;
import com.sonrisa.homework.modules.alert.repository.AlertRepository;
import com.sonrisa.homework.modules.alert.service.base.AlertService;
import com.sonrisa.homework.modules.sender.model.Sender;
import com.sonrisa.homework.modules.sender.repository.SenderRepository;
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
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final SenderRepository senderRepository;

    @Override
    @Transactional
    public AlertDTO create(AlertRequest request, RequestingUser requester) {
        if (!requester.canAccess(request.userId())) {
            throw new AccessDeniedException("Cannot create an alert for another user");
        }
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));
        Sender sender = senderRepository.findById(request.senderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found: " + request.senderId()));
        Alert alert = Alert.builder()
                .type(request.type())
                .criteria(request.criteria())
                .user(user)
                .sender(sender)
                .active(true)
                .build();
        return AlertDTO.fromEntity(alertRepository.save(alert));
    }

    @Override
    public List<AlertDTO> getActiveByUser(UUID userId, RequestingUser requester) {
        if (!requester.canAccess(userId)) {
            throw new AccessDeniedException("Cannot view another user's alerts");
        }
        return alertRepository.findByUserIdAndActiveTrue(userId).stream().map(AlertDTO::fromEntity).toList();
    }

    @Override
    public List<AlertDTO> getAllByUser(UUID userId) {
        return alertRepository.findByUserId(userId).stream().map(AlertDTO::fromEntity).toList();
    }

    @Override
    public AlertDTO getById(UUID id, RequestingUser requester) {
        Alert alert = findEntity(id);
        requireAccess(requester, alert);
        return AlertDTO.fromEntity(alert);
    }

    @Override
    @Transactional
    public AlertDTO deactivate(UUID id, RequestingUser requester) {
        Alert alert = findEntity(id);
        requireAccess(requester, alert);
        alert.setActive(false);
        return AlertDTO.fromEntity(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public void delete(UUID id, RequestingUser requester) {
        Alert alert = findEntity(id);
        requireAccess(requester, alert);
        alertRepository.delete(alert);
    }

    private void requireAccess(RequestingUser requester, Alert alert) {
        if (!requester.canAccess(alert.getUser().getId())) {
            throw new AccessDeniedException("Cannot access another user's alert");
        }
    }

    private Alert findEntity(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
    }
}
