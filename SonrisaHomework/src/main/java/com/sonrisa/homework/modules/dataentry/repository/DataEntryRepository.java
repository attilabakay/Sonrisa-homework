package com.sonrisa.homework.modules.dataentry.repository;

import com.sonrisa.homework.modules.dataentry.model.DataEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DataEntryRepository extends JpaRepository<DataEntry, UUID> {
    List<DataEntry> findByDataSourceId(UUID dataSourceId);

    boolean existsByDataSourceId(UUID dataSourceId);
}
