package com.sonrisa.homework.modules.datasource.repository;

import com.sonrisa.homework.modules.datasource.model.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DataSourceRepository extends JpaRepository<DataSource, UUID> {
    // The scheduled ingestion job must filter on this so a deactivated source actually stops
    // being polled (workflow.md §2).
    List<DataSource> findByActiveTrue();
}
