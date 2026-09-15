package com.sonrisa.homework.matching;

import com.sonrisa.homework.channel.NotificationChannel;
import com.sonrisa.homework.channel.NotificationChannelResolver;
import com.sonrisa.homework.channel.NotificationResult;
import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.common.enums.NotificationStatus;
import com.sonrisa.homework.common.enums.ResourceType;
import com.sonrisa.homework.common.enums.SenderType;
import com.sonrisa.homework.matching.sourcedata.EmergencyData;
import com.sonrisa.homework.matching.sourcedata.MarketData;
import com.sonrisa.homework.matching.sourcedata.NewsData;
import com.sonrisa.homework.matching.sourcedata.SourceDataMapper;
import com.sonrisa.homework.matching.sourcedata.WeatherData;
import com.sonrisa.homework.modules.alert.model.Alert;
import com.sonrisa.homework.modules.alert.repository.AlertRepository;
import com.sonrisa.homework.modules.datasource.model.DataSource;
import com.sonrisa.homework.modules.dataentry.model.DataEntry;
import com.sonrisa.homework.modules.notification.model.NotificationAttempt;
import com.sonrisa.homework.modules.notification.repository.NotificationAttemptRepository;
import com.sonrisa.homework.modules.sender.model.Sender;
import com.sonrisa.homework.modules.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Covers the four alert-type trigger rules and the dispatch/record behavior — the core
// ingest -> match -> notify loop mvp final.md is built to prove. SourceDataMapper's own
// field-mapping logic is covered separately (SourceDataMapperTest); here it's mocked so these
// tests only exercise MatchingEngine's own decisions.
@ExtendWith(MockitoExtension.class)
class MatchingEngineTest {

    @Mock
    private AlertRepository alertRepository;
    @Mock
    private NotificationAttemptRepository notificationAttemptRepository;
    @Mock
    private NotificationChannelResolver channelResolver;
    @Mock
    private SourceDataMapper sourceDataMapper;
    @Mock
    private NotificationChannel channel;

    private MatchingEngine matchingEngine;

    @BeforeEach
    void setUp() {
        matchingEngine = new MatchingEngine(
                alertRepository, notificationAttemptRepository, channelResolver, sourceDataMapper, new ObjectMapper());
        when(sourceDataMapper.normalize(any(), any())).thenReturn(Map.of());
    }

    private User user() {
        return User.builder().id(UUID.randomUUID()).email("user@example.com").password("hash").admin(false).active(true).build();
    }

    private Sender sender(SenderType type) {
        return Sender.builder().id(UUID.randomUUID()).type(type).user(user()).config("config").build();
    }

    private Alert alert(DataType type, String criteria, Sender sender) {
        return Alert.builder().id(UUID.randomUUID()).type(type).criteria(criteria).user(sender.getUser()).sender(sender).active(true).build();
    }

    private DataEntry entry(DataType type) {
        DataSource source = DataSource.builder()
                .id(UUID.randomUUID()).type(type).link("https://example.com").resourceType(ResourceType.JSON)
                .fieldMapping("{}").active(true).build();
        return DataEntry.builder().id(UUID.randomUUID()).type(type).dataSource(source).rawJsonData("{}").receivedAt(Instant.now()).build();
    }

    private void stubSuccessfulSend(SenderType type) {
        when(channelResolver.resolve(type)).thenReturn(channel);
        when(channel.send(any())).thenReturn(NotificationResult.success());
    }

    @Test
    void newsAlertMatchesKeywordCaseInsensitivelyAcrossHeadlineAndText() {
        Sender sender = sender(SenderType.EMAIL);
        Alert alert = alert(DataType.NEWS, "{\"keyword\":\"Rain\"}", sender);
        when(alertRepository.findMatchableAlerts(DataType.NEWS)).thenReturn(List.of(alert));
        when(sourceDataMapper.toNews(any())).thenReturn(new NewsData(null, "heavy RAIN expected", null));
        stubSuccessfulSend(SenderType.EMAIL);

        matchingEngine.evaluate(entry(DataType.NEWS));

        verify(notificationAttemptRepository).save(any(NotificationAttempt.class));
    }

    @Test
    void newsAlertDoesNotMatchWhenKeywordAbsent() {
        Sender sender = sender(SenderType.EMAIL);
        Alert alert = alert(DataType.NEWS, "{\"keyword\":\"earthquake\"}", sender);
        when(alertRepository.findMatchableAlerts(DataType.NEWS)).thenReturn(List.of(alert));
        when(sourceDataMapper.toNews(any())).thenReturn(new NewsData("Sunny weekend ahead", null, null));

        matchingEngine.evaluate(entry(DataType.NEWS));

        verify(notificationAttemptRepository, never()).save(any());
    }

    @Test
    void marketAlertMatchesAboveThreshold() {
        Sender sender = sender(SenderType.SLACK);
        Alert alert = alert(DataType.MARKET, "{\"ticker\":\"AAPL\",\"comparator\":\"ABOVE\",\"threshold\":150}", sender);
        when(alertRepository.findMatchableAlerts(DataType.MARKET)).thenReturn(List.of(alert));
        when(sourceDataMapper.toMarket(any())).thenReturn(new MarketData("AAPL", new BigDecimal("175.5"), null));
        stubSuccessfulSend(SenderType.SLACK);

        matchingEngine.evaluate(entry(DataType.MARKET));

        verify(notificationAttemptRepository).save(any());
    }

    @Test
    void marketAlertDoesNotMatchBelowThreshold() {
        Sender sender = sender(SenderType.SLACK);
        Alert alert = alert(DataType.MARKET, "{\"ticker\":\"AAPL\",\"comparator\":\"ABOVE\",\"threshold\":150}", sender);
        when(alertRepository.findMatchableAlerts(DataType.MARKET)).thenReturn(List.of(alert));
        when(sourceDataMapper.toMarket(any())).thenReturn(new MarketData("AAPL", new BigDecimal("100"), null));

        matchingEngine.evaluate(entry(DataType.MARKET));

        verify(notificationAttemptRepository, never()).save(any());
    }

    @Test
    void marketAlertIgnoresWrongTicker() {
        Sender sender = sender(SenderType.SLACK);
        Alert alert = alert(DataType.MARKET, "{\"ticker\":\"AAPL\",\"comparator\":\"ABOVE\",\"threshold\":150}", sender);
        when(alertRepository.findMatchableAlerts(DataType.MARKET)).thenReturn(List.of(alert));
        when(sourceDataMapper.toMarket(any())).thenReturn(new MarketData("GOOG", new BigDecimal("999"), null));

        matchingEngine.evaluate(entry(DataType.MARKET));

        verify(notificationAttemptRepository, never()).save(any());
    }

    @Test
    void disasterAlertMatchesRegionCaseInsensitively() {
        Sender sender = sender(SenderType.EMAIL);
        Alert alert = alert(DataType.DISASTER, "{\"region\":\"California\"}", sender);
        when(alertRepository.findMatchableAlerts(DataType.DISASTER)).thenReturn(List.of(alert));
        when(sourceDataMapper.toEmergency(any())).thenReturn(new EmergencyData("california", "wildfire", "high", null));
        stubSuccessfulSend(SenderType.EMAIL);

        matchingEngine.evaluate(entry(DataType.DISASTER));

        verify(notificationAttemptRepository).save(any());
    }

    @Test
    void disasterAlertIgnoresNonMatchingRegion() {
        Sender sender = sender(SenderType.EMAIL);
        Alert alert = alert(DataType.DISASTER, "{\"region\":\"California\"}", sender);
        when(alertRepository.findMatchableAlerts(DataType.DISASTER)).thenReturn(List.of(alert));
        when(sourceDataMapper.toEmergency(any())).thenReturn(new EmergencyData("Texas", "flood", "medium", null));

        matchingEngine.evaluate(entry(DataType.DISASTER));

        verify(notificationAttemptRepository, never()).save(any());
    }

    @Test
    void weatherAlertRequiresRegionMatchAndRain_neitherAloneIsEnough() {
        Sender sender = sender(SenderType.EMAIL);
        Alert alert = alert(DataType.WEATHER, "{\"region\":\"California\"}", sender);
        when(alertRepository.findMatchableAlerts(DataType.WEATHER)).thenReturn(List.of(alert));

        // right region, no rain -> no match
        when(sourceDataMapper.toWeather(any())).thenReturn(new WeatherData("California", "sunny", null));
        matchingEngine.evaluate(entry(DataType.WEATHER));
        verify(notificationAttemptRepository, never()).save(any());

        // rain, wrong region -> no match
        when(sourceDataMapper.toWeather(any())).thenReturn(new WeatherData("Texas", "heavy rain", null));
        matchingEngine.evaluate(entry(DataType.WEATHER));
        verify(notificationAttemptRepository, never()).save(any());
    }

    @Test
    void weatherAlertMatchesWhenBothRegionAndRainAreSatisfied() {
        Sender sender = sender(SenderType.EMAIL);
        Alert alert = alert(DataType.WEATHER, "{\"region\":\"California\"}", sender);
        when(alertRepository.findMatchableAlerts(DataType.WEATHER)).thenReturn(List.of(alert));
        when(sourceDataMapper.toWeather(any())).thenReturn(new WeatherData("California", "light rain", null));
        stubSuccessfulSend(SenderType.EMAIL);

        matchingEngine.evaluate(entry(DataType.WEATHER));

        verify(notificationAttemptRepository).save(any());
    }

    @Test
    void dispatchRecordsFailedStatusAndErrorWhenChannelReportsFailure() {
        Sender sender = sender(SenderType.EMAIL);
        Alert alert = alert(DataType.NEWS, "{\"keyword\":\"rain\"}", sender);
        when(alertRepository.findMatchableAlerts(DataType.NEWS)).thenReturn(List.of(alert));
        when(sourceDataMapper.toNews(any())).thenReturn(new NewsData("rain today", null, null));
        when(channelResolver.resolve(SenderType.EMAIL)).thenReturn(channel);
        when(channel.send(any())).thenReturn(NotificationResult.failure("SMTP refused"));

        ArgumentCaptor<NotificationAttempt> captor = ArgumentCaptor.forClass(NotificationAttempt.class);
        matchingEngine.evaluate(entry(DataType.NEWS));
        verify(notificationAttemptRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(captor.getValue().getError()).isEqualTo("SMTP refused");
    }

    @Test
    void dispatchCatchesChannelExceptionsAndStillRecordsAFailedAttempt() {
        Sender sender = sender(SenderType.EMAIL);
        Alert alert = alert(DataType.NEWS, "{\"keyword\":\"rain\"}", sender);
        when(alertRepository.findMatchableAlerts(DataType.NEWS)).thenReturn(List.of(alert));
        when(sourceDataMapper.toNews(any())).thenReturn(new NewsData("rain today", null, null));
        when(channelResolver.resolve(SenderType.EMAIL)).thenReturn(channel);
        when(channel.send(any())).thenThrow(new RuntimeException("boom"));

        ArgumentCaptor<NotificationAttempt> captor = ArgumentCaptor.forClass(NotificationAttempt.class);
        matchingEngine.evaluate(entry(DataType.NEWS));
        verify(notificationAttemptRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(captor.getValue().getError()).isEqualTo("boom");
    }

    @Test
    void noMatchableAlertsMeansNoDispatchAtAll() {
        when(alertRepository.findMatchableAlerts(DataType.NEWS)).thenReturn(List.of());

        matchingEngine.evaluate(entry(DataType.NEWS));

        verify(channelResolver, never()).resolve(any());
        verify(notificationAttemptRepository, never()).save(any());
    }
}
