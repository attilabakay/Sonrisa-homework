package com.sonrisa.homework.modules.dataentry.service.base;

import com.sonrisa.homework.modules.dataentry.dto.base.DataEntryDTO;
import com.sonrisa.homework.modules.dataentry.dto.request.DataEntryRequest;

import java.util.List;
import java.util.UUID;

public interface DataEntryService {

    DataEntryDTO create(DataEntryRequest request);

    List<DataEntryDTO> getAll();

    List<DataEntryDTO> getBySource(UUID dataSourceId);

    DataEntryDTO getById(UUID id);
}
