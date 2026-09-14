package com.sonrisa.homework.modules.datasource.model;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.common.enums.ResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "data_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataSource {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataType type;

    @Column(nullable = false)
    private String link;

    @Column(name = "api_key")
    private String apiKey;

    // Provider's raw field names -> interface field names (mvp final.md §5 mapping UI).
    @Column(name = "field_mapping", columnDefinition = "TEXT")
    private String fieldMapping;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
