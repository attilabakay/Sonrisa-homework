package com.sonrisa.homework.modules.alert.service.impl;

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
    public AlertDTO create(AlertRequest request) {
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
    public List<AlertDTO> getActiveByUser(UUID userId) {
        return alertRepository.findByUserIdAndActiveTrue(userId).stream().map(AlertDTO::fromEntity).toList();
    }

    @Override
    public List<AlertDTO> getAllByUser(UUID userId) {
        return alertRepository.findByUserId(userId).stream().map(AlertDTO::fromEntity).toList();
    }

    @Override
    public AlertDTO getById(UUID id) {
        return AlertDTO.fromEntity(findEntity(id));
    }

    @Override
    @Transactional
    public AlertDTO deactivate(UUID id) {
        Alert alert = findEntity(id);
        alert.setActive(false);
        return AlertDTO.fromEntity(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Alert alert = findEntity(id);
        alertRepository.delete(alert);
    }

    private Alert findEntity(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
    }
}
