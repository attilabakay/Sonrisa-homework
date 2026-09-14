package com.sonrisa.homework.ingestion;

import com.sonrisa.homework.common.enums.ResourceType;
import com.sonrisa.homework.common.exception.ResourceNotFoundException;
import com.sonrisa.homework.modules.dataentry.dto.base.DataEntryDTO;
import com.sonrisa.homework.modules.dataentry.dto.request.DataEntryRequest;
import com.sonrisa.homework.modules.dataentry.service.base.DataEntryService;
import com.sonrisa.homework.modules.datasource.model.DataSource;
import com.sonrisa.homework.modules.datasource.repository.DataSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Manual trigger standing in for the not-yet-built polling job (mvp final.md §4) — fetches
// DataSource.link once and turns the response into one or more DataEntry rows, reusing
// DataEntryService.create so the "creation triggers Matching" contract (workflow.md §3) still
// holds regardless of where the entry came from.
@Service
@RequiredArgsConstructor
public class DataSourceIngestionService {

    private final DataSourceRepository dataSourceRepository;
    private final DataEntryService dataEntryService;
    private final RssFeedParser rssFeedParser;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public List<DataEntryDTO> ingest(UUID dataSourceId) {
        DataSource dataSource = dataSourceRepository.findById(dataSourceId)
                .orElseThrow(() -> new ResourceNotFoundException("DataSource not found: " + dataSourceId));

        String body = restClient.get().uri(dataSource.getLink()).retrieve().body(String.class);

        // XML feeds (e.g. RSS) bundle many events in one response — split into one DataEntry
        // per item so matching still operates on a single event at a time, same as a JSON
        // provider that returns one object per poll.
        List<String> rawEntries = dataSource.getResourceType() == ResourceType.XML
                ? rssFeedParser.parseItems(body).stream().map(this::toJson).toList()
                : List.of(body);

        return rawEntries.stream()
                .map(raw -> dataEntryService.create(new DataEntryRequest(dataSourceId, raw)))
                .toList();
    }

    private String toJson(Map<String, String> fields) {
        return objectMapper.writeValueAsString(fields);
    }
}
