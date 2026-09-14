package com.sonrisa.homework.modules.dataentry.model;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.modules.datasource.model.DataSource;
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
@Table(name = "data_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    // Stored as-is; immutable by design so fieldMapping changes stay re-interpretable at read-time.
    @Column(name = "raw_json_data", nullable = false, columnDefinition = "TEXT")
    private String rawJsonData;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}
