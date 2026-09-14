package com.sonrisa.homework.modules.dataentry.service.impl;

import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.datasource.model.DataSource;
import com.sonrisa.homework.modules.datasource.repository.DataSourceRepository;
import com.sonrisa.homework.modules.dataentry.dto.base.DataEntryDTO;
import com.sonrisa.homework.modules.dataentry.dto.request.DataEntryRequest;
import com.sonrisa.homework.modules.dataentry.model.DataEntry;
import com.sonrisa.homework.modules.dataentry.repository.DataEntryRepository;
import com.sonrisa.homework.modules.dataentry.service.base.DataEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataEntryServiceImpl implements DataEntryService {

    private final DataEntryRepository dataEntryRepository;
    private final DataSourceRepository dataSourceRepository;

    @Override
    @Transactional
    public DataEntryDTO create(DataEntryRequest request) {
        DataSource dataSource = dataSourceRepository.findById(request.dataSourceId())
                .orElseThrow(() -> new ResourceNotFoundException("DataSource not found: " + request.dataSourceId()));
        DataEntry entry = DataEntry.builder()
                .type(dataSource.getType())
                .dataSource(dataSource)
                .rawJsonData(request.rawJsonData())
                .receivedAt(Instant.now())
                .build();
        return DataEntryDTO.fromEntity(dataEntryRepository.save(entry));
    }

    @Override
    public List<DataEntryDTO> getAll() {
        return dataEntryRepository.findAll().stream().map(DataEntryDTO::fromEntity).toList();
    }

    @Override
    public List<DataEntryDTO> getBySource(UUID dataSourceId) {
        return dataEntryRepository.findByDataSourceId(dataSourceId).stream().map(DataEntryDTO::fromEntity).toList();
    }

    @Override
    public DataEntryDTO getById(UUID id) {
        return DataEntryDTO.fromEntity(dataEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataEntry not found: " + id)));
    }
}
