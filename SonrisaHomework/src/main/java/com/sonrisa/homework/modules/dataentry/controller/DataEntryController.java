package com.sonrisa.homework.modules.dataentry.controller;

import com.sonrisa.homework.modules.dataentry.dto.base.DataEntryDTO;
import com.sonrisa.homework.modules.dataentry.dto.request.DataEntryRequest;
import com.sonrisa.homework.modules.dataentry.service.base.DataEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// No update/delete: dataEntry rows are immutable, append-only ingestion log (workflow.md §3).
@RestController
@RequestMapping("/api/data-entries")
@RequiredArgsConstructor
public class DataEntryController {

    private final DataEntryService dataEntryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataEntryDTO create(@Valid @RequestBody DataEntryRequest request) {
        return dataEntryService.create(request);
    }

    @GetMapping
    public List<DataEntryDTO> getAll() {
        return dataEntryService.getAll();
    }

    @GetMapping("/source/{dataSourceId}")
    public List<DataEntryDTO> getBySource(@PathVariable UUID dataSourceId) {
        return dataEntryService.getBySource(dataSourceId);
    }

    @GetMapping("/{id}")
    public DataEntryDTO getById(@PathVariable UUID id) {
        return dataEntryService.getById(id);
    }
}
