package com.sonrisa.homework.modules.alert.repository;

import com.sonrisa.homework.modules.alert.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByUserIdAndActiveTrue(UUID userId);
}
