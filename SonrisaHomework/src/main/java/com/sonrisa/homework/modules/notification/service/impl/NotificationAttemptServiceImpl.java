package com.sonrisa.homework.modules.notification.service.impl;

import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.alert.model.Alert;
import com.sonrisa.homework.modules.alert.repository.AlertRepository;
import com.sonrisa.homework.modules.dataentry.model.DataEntry;
import com.sonrisa.homework.modules.dataentry.repository.DataEntryRepository;
import com.sonrisa.homework.modules.notification.dto.base.NotificationAttemptDTO;
import com.sonrisa.homework.modules.notification.dto.request.NotificationAttemptRequest;
import com.sonrisa.homework.modules.notification.model.NotificationAttempt;
import com.sonrisa.homework.modules.notification.repository.NotificationAttemptRepository;
import com.sonrisa.homework.modules.notification.service.base.NotificationAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationAttemptServiceImpl implements NotificationAttemptService {

    private final NotificationAttemptRepository notificationAttemptRepository;
    private final AlertRepository alertRepository;
    private final DataEntryRepository dataEntryRepository;

    @Override
    @Transactional
    public NotificationAttemptDTO create(NotificationAttemptRequest request) {
        Alert alert = alertRepository.findById(request.alertId())
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + request.alertId()));
        DataEntry dataEntry = dataEntryRepository.findById(request.dataEntryId())
                .orElseThrow(() -> new ResourceNotFoundException("DataEntry not found: " + request.dataEntryId()));
        NotificationAttempt attempt = NotificationAttempt.builder()
                .alert(alert)
                .dataEntry(dataEntry)
                .status(request.status())
                .error(request.error())
                .sentAt(Instant.now())
                .build();
        return NotificationAttemptDTO.fromEntity(notificationAttemptRepository.save(attempt));
    }

    @Override
    public List<NotificationAttemptDTO> getAll() {
        return notificationAttemptRepository.findAll().stream().map(NotificationAttemptDTO::fromEntity).toList();
    }

    @Override
    public List<NotificationAttemptDTO> getByAlert(UUID alertId) {
        return notificationAttemptRepository.findByAlertId(alertId).stream().map(NotificationAttemptDTO::fromEntity).toList();
    }
}
