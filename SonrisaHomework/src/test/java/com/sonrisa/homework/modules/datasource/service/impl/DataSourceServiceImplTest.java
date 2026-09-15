package com.sonrisa.homework.modules.datasource.service.impl;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.common.enums.ResourceType;
import com.sonrisa.homework.common.exception.ConflictException;
import com.sonrisa.homework.modules.datasource.dto.request.DataSourceRequest;
import com.sonrisa.homework.modules.datasource.model.DataSource;
import com.sonrisa.homework.modules.datasource.repository.DataSourceRepository;
import com.sonrisa.homework.modules.dataentry.repository.DataEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSourceServiceImplTest {

    @Mock
    private DataSourceRepository dataSourceRepository;
    @Mock
    private DataEntryRepository dataEntryRepository;

    private DataSourceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DataSourceServiceImpl(dataSourceRepository, dataEntryRepository);
    }

    private DataSource dataSource() {
        return DataSource.builder().id(UUID.randomUUID()).type(DataType.NEWS).link("https://example.com/feed")
                .resourceType(ResourceType.JSON).fieldMapping("{}").active(true).build();
    }

    @Test
    void deleteIsBlockedWhenTheSourceHasExistingDataEntryHistory() {
        DataSource source = dataSource();
        when(dataSourceRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(dataEntryRepository.existsByDataSourceId(source.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.delete(source.getId()))
                .isInstanceOf(ConflictException.class);
        verify(dataSourceRepository, never()).delete(any());
    }

    @Test
    void deleteSucceedsWhenNoDataEntryHistoryExists() {
        DataSource source = dataSource();
        when(dataSourceRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(dataEntryRepository.existsByDataSourceId(source.getId())).thenReturn(false);

        service.delete(source.getId());

        verify(dataSourceRepository).delete(source);
    }

    @Test
    void deactivateFlipsActiveToFalse() {
        DataSource source = dataSource();
        when(dataSourceRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(dataSourceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.deactivate(source.getId());

        assertThat(result.active()).isFalse();
    }

    @Test
    void activateFlipsActiveBackToTrue() {
        DataSource source = dataSource();
        source.setActive(false);
        when(dataSourceRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(dataSourceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.activate(source.getId());

        assertThat(result.active()).isTrue();
    }

    @Test
    void updateReplacesTheEditableFields() {
        DataSource source = dataSource();
        when(dataSourceRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(dataSourceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        DataSourceRequest request = new DataSourceRequest(
                DataType.MARKET, "https://example.com/new-feed", "new-key", "{\"a\":\"b\"}", ResourceType.XML);

        var result = service.update(source.getId(), request);

        assertThat(result.type()).isEqualTo(DataType.MARKET);
        assertThat(result.link()).isEqualTo("https://example.com/new-feed");
        assertThat(result.apiKey()).isEqualTo("new-key");
        assertThat(result.fieldMapping()).isEqualTo("{\"a\":\"b\"}");
        assertThat(result.resourceType()).isEqualTo(ResourceType.XML);
    }
}
