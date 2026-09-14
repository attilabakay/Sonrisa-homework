package com.sonrisa.homework.modules.datasource.controller;

import com.sonrisa.homework.modules.datasource.dto.base.DataSourceDTO;
import com.sonrisa.homework.modules.datasource.dto.request.DataSourceRequest;
import com.sonrisa.homework.modules.datasource.service.base.DataSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/data-sources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataSourceDTO create(@Valid @RequestBody DataSourceRequest request) {
        return dataSourceService.create(request);
    }

    @GetMapping
    public List<DataSourceDTO> getAll() {
        return dataSourceService.getAll();
    }

    @GetMapping("/{id}")
    public DataSourceDTO getById(@PathVariable UUID id) {
        return dataSourceService.getById(id);
    }

    @PutMapping("/{id}")
    public DataSourceDTO update(@PathVariable UUID id, @Valid @RequestBody DataSourceRequest request) {
        return dataSourceService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public DataSourceDTO deactivate(@PathVariable UUID id) {
        return dataSourceService.deactivate(id);
    }

    // Only safe when no dataEntry history exists for this source (workflow.md §2) — otherwise 409.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        dataSourceService.delete(id);
    }
}
