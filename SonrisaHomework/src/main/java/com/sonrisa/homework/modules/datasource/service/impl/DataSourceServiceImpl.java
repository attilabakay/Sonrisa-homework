package com.sonrisa.homework.modules.datasource.service.impl;

import com.sonrisa.homework.common.exception.ConflictException;
import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.datasource.dto.base.DataSourceDTO;
import com.sonrisa.homework.modules.datasource.dto.request.DataSourceRequest;
import com.sonrisa.homework.modules.datasource.model.DataSource;
import com.sonrisa.homework.modules.datasource.repository.DataSourceRepository;
import com.sonrisa.homework.modules.datasource.service.base.DataSourceService;
import com.sonrisa.homework.modules.dataentry.repository.DataEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceRepository dataSourceRepository;
    private final DataEntryRepository dataEntryRepository;

    @Override
    @Transactional
    public DataSourceDTO create(DataSourceRequest request) {
        DataSource dataSource = DataSource.builder()
                .type(request.type())
                .link(request.link())
                .apiKey(request.apiKey())
                .fieldMapping(request.fieldMapping())
                .resourceType(request.resourceType())
                .active(true)
                .build();
        return DataSourceDTO.fromEntity(dataSourceRepository.save(dataSource));
    }

    @Override
    public List<DataSourceDTO> getAll() {
        return dataSourceRepository.findAll().stream().map(DataSourceDTO::fromEntity).toList();
    }

    @Override
    public DataSourceDTO getById(UUID id) {
        return DataSourceDTO.fromEntity(findEntity(id));
    }

    @Override
    @Transactional
    public DataSourceDTO update(UUID id, DataSourceRequest request) {
        DataSource dataSource = findEntity(id);
        dataSource.setType(request.type());
        dataSource.setLink(request.link());
        dataSource.setApiKey(request.apiKey());
        dataSource.setFieldMapping(request.fieldMapping());
        dataSource.setResourceType(request.resourceType());
        return DataSourceDTO.fromEntity(dataSourceRepository.save(dataSource));
    }

    @Override
    @Transactional
    public DataSourceDTO deactivate(UUID id) {
        DataSource dataSource = findEntity(id);
        dataSource.setActive(false);
        return DataSourceDTO.fromEntity(dataSourceRepository.save(dataSource));
    }

    @Override
    @Transactional
    public DataSourceDTO activate(UUID id) {
        DataSource dataSource = findEntity(id);
        dataSource.setActive(true);
        return DataSourceDTO.fromEntity(dataSourceRepository.save(dataSource));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        DataSource dataSource = findEntity(id);
        if (dataEntryRepository.existsByDataSourceId(id)) {
            throw new ConflictException("Cannot delete a data source with existing dataEntry history; deactivate it instead: " + id);
        }
        dataSourceRepository.delete(dataSource);
    }

    private DataSource findEntity(UUID id) {
        return dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataSource not found: " + id));
    }
}
