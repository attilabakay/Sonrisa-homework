package com.sonrisa.homework.bootstrap;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.common.enums.ResourceType;
import com.sonrisa.homework.common.enums.SenderType;
import com.sonrisa.homework.modules.alert.model.Alert;
import com.sonrisa.homework.modules.alert.repository.AlertRepository;
import com.sonrisa.homework.modules.datasource.model.DataSource;
import com.sonrisa.homework.modules.datasource.repository.DataSourceRepository;
import com.sonrisa.homework.modules.sender.model.Sender;
import com.sonrisa.homework.modules.sender.repository.SenderRepository;
import com.sonrisa.homework.modules.user.model.User;
import com.sonrisa.homework.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Opt-in via app.demo.enabled (off by default — this is sample content, not a default
// deployment state). Seeds two demo users (one disabled, to show that state), a sender +
// one alert per type for the active one, and the two real NEWS providers used throughout
// this project's own testing — proving mvp final.md §4's "second provider within a category
// is a mapping exercise" with real, live data rather than a synthetic example.
@Component
@ConditionalOnProperty(prefix = "app.demo", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "password123";
    private static final String ALICE_EMAIL = "alice@example.com";
    private static final String BOB_EMAIL = "bob@example.com";

    private final UserRepository userRepository;
    private final SenderRepository senderRepository;
    private final AlertRepository alertRepository;
    private final DataSourceRepository dataSourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(ALICE_EMAIL)) {
            log.info("Demo data already present, skipping seed.");
            return;
        }

        User alice = userRepository.save(User.builder()
                .email(ALICE_EMAIL)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .admin(false)
                .active(true)
                .build());

        // Disabled on purpose — demonstrates the admin enable/disable flow (mvp final.md §1)
        // and that a disabled user's login is rejected outright (not just their alerts paused).
        userRepository.save(User.builder()
                .email(BOB_EMAIL)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .admin(false)
                .active(false)
                .build());

        Sender emailSender = senderRepository.save(Sender.builder()
                .type(SenderType.EMAIL)
                .user(alice)
                .config(ALICE_EMAIL)
                .build());
        Sender slackSender = senderRepository.save(Sender.builder()
                .type(SenderType.SLACK)
                .user(alice)
                .config("https://hooks.slack.com/services/DEMO/WEBHOOK")
                .build());

        alertRepository.save(Alert.builder()
                .type(DataType.NEWS)
                .criteria("{\"keyword\":\"the\"}")
                .user(alice)
                .sender(emailSender)
                .active(true)
                .build());
        alertRepository.save(Alert.builder()
                .type(DataType.MARKET)
                .criteria("{\"ticker\":\"AAPL\",\"comparator\":\"ABOVE\",\"threshold\":150}")
                .user(alice)
                .sender(slackSender)
                .active(true)
                .build());
        alertRepository.save(Alert.builder()
                .type(DataType.DISASTER)
                .criteria("{\"region\":\"California\"}")
                .user(alice)
                .sender(emailSender)
                .active(true)
                .build());

        dataSourceRepository.save(DataSource.builder()
                .type(DataType.NEWS)
                .link("https://feeds.bbci.co.uk/news/rss.xml")
                .resourceType(ResourceType.XML)
                .fieldMapping("{\"title\":\"headline\",\"description\":\"text\",\"pubDate\":\"timestamp\"}")
                .active(true)
                .build());
        dataSourceRepository.save(DataSource.builder()
                .type(DataType.NEWS)
                .link("https://saurav.tech/NewsAPI/everything/cnn.json")
                .resourceType(ResourceType.JSON)
                .fieldMapping("{\"title\":\"headline\",\"description\":\"text\",\"publishedAt\":\"timestamp\"}")
                .active(true)
                .build());

        log.info("Seeded demo data: {} / {} (active), {} / {} (disabled), "
                        + "2 senders, 3 alerts, 2 live NEWS data sources (BBC RSS + CNN NewsAPI).",
                ALICE_EMAIL, DEMO_PASSWORD, BOB_EMAIL, DEMO_PASSWORD);
    }
}
