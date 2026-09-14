package com.sonrisa.homework.modules.notification.repository;

import com.sonrisa.homework.modules.notification.model.NotificationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationAttemptRepository extends JpaRepository<NotificationAttempt, UUID> {
    List<NotificationAttempt> findByAlertId(UUID alertId);
}
