package com.sonrisa.homework.modules.datasource.service.base;

import com.sonrisa.homework.modules.datasource.dto.base.DataSourceDTO;
import com.sonrisa.homework.modules.datasource.dto.request.DataSourceRequest;

import java.util.List;
import java.util.UUID;

public interface DataSourceService {

    DataSourceDTO create(DataSourceRequest request);

    List<DataSourceDTO> getAll();

    DataSourceDTO getById(UUID id);

    DataSourceDTO update(UUID id, DataSourceRequest request);

    DataSourceDTO deactivate(UUID id);

    void delete(UUID id);
}
