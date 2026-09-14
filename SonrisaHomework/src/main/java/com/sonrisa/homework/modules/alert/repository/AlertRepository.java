package com.sonrisa.homework.modules.alert.repository;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.modules.alert.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByUserIdAndActiveTrue(UUID userId);

    List<Alert> findByUserId(UUID userId);

    // A disabled user's existing alerts must stop firing (workflow.md §1) — enforced here,
    // at matching time, not just at login.
    @Query("select a from Alert a where a.type = :type and a.active = true and a.user.active = true")
    List<Alert> findMatchableAlerts(@Param("type") DataType type);
}
