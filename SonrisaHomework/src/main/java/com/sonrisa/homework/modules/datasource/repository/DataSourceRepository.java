package com.sonrisa.homework.modules.datasource.repository;

import com.sonrisa.homework.modules.datasource.model.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DataSourceRepository extends JpaRepository<DataSource, UUID> {
}
