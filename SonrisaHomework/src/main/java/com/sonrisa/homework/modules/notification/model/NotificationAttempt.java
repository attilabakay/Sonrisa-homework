package com.sonrisa.homework.modules.notification.model;

import com.sonrisa.homework.common.enums.NotificationStatus;
import com.sonrisa.homework.modules.alert.model.Alert;
import com.sonrisa.homework.modules.dataentry.model.DataEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationAttempt {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    // The entry that triggered this send.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_entry_id", nullable = false)
    private DataEntry dataEntry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private String error;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
}
