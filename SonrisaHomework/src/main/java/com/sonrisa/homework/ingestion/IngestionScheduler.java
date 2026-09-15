package com.sonrisa.homework.ingestion;

import com.sonrisa.homework.modules.datasource.model.DataSource;
import com.sonrisa.homework.modules.datasource.repository.DataSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// The polling job mvp final.md §4 calls for, kept deliberately simple: one global interval
// (IngestionProperties) for every active DataSource, no per-source scheduling. Reuses
// DataSourceIngestionService.ingest(), which already creates DataEntry rows through
// DataEntryService.create() -- so "after ingestion, check alerts and notify" falls out of the
// existing MatchingEngine wiring for free, same as the manual "Ingest now" button.
@Component
@RequiredArgsConstructor
@Slf4j
public class IngestionScheduler {

    private final DataSourceRepository dataSourceRepository;
    private final DataSourceIngestionService dataSourceIngestionService;

    @Scheduled(initialDelay = 10_000, fixedDelayString = "#{ingestionProperties.effectiveIntervalMillis}")
    public void pollActiveSources() {
        List<DataSource> activeSources = dataSourceRepository.findByActiveTrue();
        log.info("Scheduled ingestion run starting for {} active data source(s)", activeSources.size());

        for (DataSource dataSource : activeSources) {
            try {
                var entries = dataSourceIngestionService.ingest(dataSource.getId());
                log.info("Ingested {} entr{} from {} ({})",
                        entries.size(), entries.size() == 1 ? "y" : "ies", dataSource.getLink(), dataSource.getType());
            } catch (Exception e) {
                // One source failing (bad URL, network error, malformed response) shouldn't
                // stop the rest of the run.
                log.warn("Scheduled ingestion failed for data source {} ({}): {}",
                        dataSource.getId(), dataSource.getLink(), e.getMessage());
            }
        }
    }
}
