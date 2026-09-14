package com.sonrisa.homework.modules.notification.service.base;

import com.sonrisa.homework.modules.notification.dto.base.NotificationAttemptDTO;
import com.sonrisa.homework.modules.notification.dto.request.NotificationAttemptRequest;

import java.util.List;
import java.util.UUID;

// Append-only audit trail — no update or delete (workflow.md §5).
public interface NotificationAttemptService {

    NotificationAttemptDTO create(NotificationAttemptRequest request);

    List<NotificationAttemptDTO> getAll();

    List<NotificationAttemptDTO> getByAlert(UUID alertId);
}
