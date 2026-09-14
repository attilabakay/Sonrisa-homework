
new discussion to shrink context first promt with the final mvp.md: I'm working on this project and starting to plan the data models. Could you give me a suggestion for the data models? I will use springboot for the backend. 

Here's a data model that maps directly to the abstractions in your MVP doc — channel adapter interface, source interface + mapping pattern, and the three fixed alert types.

## Core design decision

Alerts and data sources both need to stay *typed* (per §2/§4) while avoiding table-per-type joins you don't have time for. The cleanest fit for a 24h build: **one table per aggregate, with a JSON column for the type-specific bits**, keyed by a discriminator enum. Postgres `jsonb` (or `JSON` type via Hibernate's `@JdbcTypeCode(SqlTypes.JSON)`) works well here; H2 works too if you're keeping it simple for the demo.

## Entities

**User**
```java
@Entity
class User {
    UUID id;
    String email;          // unique
    String passwordHash;
    boolean enabled;        // admin toggle, §1
    boolean isAdmin;
    Instant createdAt;
}
```

**Alert** — one row per alert, regardless of type
```java
@Entity
class Alert {
    UUID id;
    @ManyToOne User user;

    @Enumerated(EnumType.STRING)
    AlertType type;         // KEYWORD, MARKET, LOCATION

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> criteria;
    // KEYWORD: {"keyword": "..."}
    // MARKET: {"ticker": "...", "comparator": "ABS"|"PCT", "threshold": 5.0, "direction": "UP"|"DOWN"}
    // LOCATION: {"region": "..."}  // named-region match per §2

    @Enumerated(EnumType.STRING)
    ChannelType channelType; // EMAIL, SLACK

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> channelConfig;
    // EMAIL: {"address": "..."}
    // SLACK: {"webhookUrl": "..."} or {"channelId": "..."}

    boolean active;
    Instant createdAt;
}
```

Using JSON for `criteria` keeps the matching engine's dispatch simple (`switch(type)` → deserialize into a small typed record per branch) without a `MarketAlert`/`KeywordAlert`/`LocationAlert` table hierarchy you'd have to join against. It also means edit-as-delete-and-recreate (deferred per §2) requires zero schema change later if you ever add real editing.

**DataSource** — the admin-configured, mapped provider (§4/§5)
```java
@Entity
class DataSource {
    UUID id;
    String name;             // display name, e.g. "NewsAPI"

    @Enumerated(EnumType.STRING)
    SourceCategory category;  // NEWS, MARKET, EMERGENCY, WEATHER

    String providerBaseUrl;
    String apiKeyRef;          // reference/secret name, not the raw key in plaintext ideally

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, String> fieldMapping;
    // maps provider's raw field names -> interface field names
    // e.g. MARKET: {"symbol": "ticker", "price": "value", "ts": "timestamp"}

    int pollIntervalSeconds;
    boolean enabled;
}
```

The `fieldMapping` is what makes "add a second provider in an existing category" a config row, not new code (§5's core promise) — your polling job reads this map and normalizes the raw response into the fixed interface shape (`MarketInterface`, `EmergencyInterface`, etc.) before it ever reaches the matching engine.

**EventLogEntry** — admin visibility log (§5), and what the matcher runs against
```java
@Entity
class EventLogEntry {
    UUID id;
    @ManyToOne DataSource dataSource;

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> rawPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> normalizedPayload;  // post-mapping, in interface shape

    Instant receivedAt;
}
```

**NotificationAttempt** — records each channel adapter call (ties to §3's call-success status)
```java
@Entity
class NotificationAttempt {
    UUID id;
    @ManyToOne Alert alert;
    @ManyToOne EventLogEntry triggeringEvent;

    @Enumerated(EnumType.STRING)
    ChannelType channel;

    @Enumerated(EnumType.STRING)
    Status status;   // SENT, FAILED

    String error;    // nullable
    Instant sentAt;
}
```

This isn't user-facing alert history (that's explicitly deferred) — it's the thing your `NotificationChannel.send(payload)` adapter writes to after every call, and it doubles as the debugging trail if a demo run silently fails.

## Why this shape works for your constraints

- **Channel adapters stay decoupled**: `NotificationChannel` is a plain interface (`EmailChannel`, `SlackChannel` implementing it); neither `Alert` nor `NotificationAttempt` needs to change when you add a third adapter — only `channelType`'s enum grows and a new `case` in the dispatcher.
- **Matching engine stays generic per type**: it reads `Alert.criteria` typed by `Alert.type`, and reads `EventLogEntry.normalizedPayload` typed by `DataSource.category` — the JSON columns are exactly the boundary where "generic field engine" (§6, deferred) would eventually slot in without touching `User`, `NotificationAttempt`, or the channel layer at all.
- **The one flagged open question** (value-change needing a previous value, not a snapshot) stays contained: right now `MARKET` criteria can carry `threshold`/`comparator` and your matcher just needs the *previous* `EventLogEntry` for that ticker to diff against — no schema change needed for MVP, just a query (`findTopByDataSourceAndCriteriaTickerOrderByReceivedAtDesc` or similar) when evaluating a market alert.

One thing worth deciding now rather than mid-build: whether `channelConfig` secrets (Slack webhook URLs) get stored in plaintext in that JSON column or referenced indirectly — for a 24h demo plaintext is probably fine, but worth being deliberate about rather than accidental.

---
prompt: I think we need data entires as weill in the data structure from the data sources. With separete interfaces per type.
---

Good call — that maps more directly onto §4's actual intent: *"Each interface defines the fixed set of fields the matching engine understands for that category."* A single JSON blob technically stores everything, but it pushes the field-shape contract into application code instead of the schema, and you lose typed queries (e.g. "give me the last `MarketEvent` for this ticker") without deserializing JSON every time. Let's split it into one table per category.

## Typed source interfaces → typed entities

Define the plain Java interfaces from §4 as the contract, then have each entity implement its interface. This keeps the matching engine programmed against the interface, not the entity:

```java
interface NewsInterface {
    String getHeadline();
    String getBody();
    Instant getTimestamp();
}

interface MarketInterface {
    String getTicker();
    BigDecimal getValue();
    Instant getTimestamp();
}

interface EmergencyInterface {
    String getRegion();
    String getEventType();
    String getSeverity();
    Instant getTimestamp();
}

interface WeatherInterface {   // stretch goal, §4
    String getRegion();
    String getCondition();
    BigDecimal getTemperature();
    Instant getTimestamp();
}
```

## Shared fields via `@MappedSuperclass`

Every event row needs an id, which source produced it, when it was ingested, and the raw payload for the admin debugging log (§5) — regardless of category:

```java
@MappedSuperclass
abstract class SourceEvent {
    @Id UUID id;
    @ManyToOne DataSource dataSource;
    Instant receivedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> rawPayload;   // pre-mapping, kept for admin log / debugging
}
```

This isn't an `@Entity` itself — no shared table, no polymorphic queries. Each subtype gets its own table, which is the right call here since the categories don't share query patterns.

## The four entities

```java
@Entity
class NewsEvent extends SourceEvent implements NewsInterface {
    String headline;
    @Column(length = 4000) String body;
    Instant timestamp;
}

@Entity
class MarketEvent extends SourceEvent implements MarketInterface {
    String ticker;
    BigDecimal value;
    Instant timestamp;
}

@Entity
class EmergencyEvent extends SourceEvent implements EmergencyInterface {
    String region;
    String eventType;
    String severity;
    Instant timestamp;
}

@Entity
class WeatherEvent extends SourceEvent implements WeatherInterface {   // stretch
    String region;
    String condition;
    BigDecimal temperature;
    Instant timestamp;
}
```

Each gets a plain Spring Data repository — `MarketEventRepository extends JpaRepository<MarketEvent, UUID>` — and now you get real typed queries for free, which matters specifically for market alerts:

```java
Optional<MarketEvent> findTopByTickerOrderByTimestampDesc(String ticker);
```

That's the previous-value lookup the value-change comparator needs — no JSON path querying required.

## What changes from before

- **`DataSource.fieldMapping`** now maps provider fields → whichever entity's typed columns apply for that `category`, instead of → generic JSON keys. Same config-not-code story from §5, just landing on real columns at ingest time.
- **`EventLogEntry` is gone**, replaced by the four typed tables above. The admin "recent events per source" view (§5) becomes a query per category (or a small union in the service layer) rather than one table scan — for MVP scale that's a non-issue.
- **`NotificationAttempt.triggeringEvent`** can't be a single JPA `@ManyToOne` anymore, since the event might live in any of four tables. Simplest MVP-safe fix: store it as a loose reference rather than an enforced FK —
  ```java
  UUID triggeringEventId;
  @Enumerated(EnumType.STRING) SourceCategory triggeringEventCategory;
  ```
  and resolve it in the service layer when displaying admin logs. Not referentially enforced by the DB, but that's a reasonable trade for a 24-hour build — tightening it later (e.g. a real polymorphic association) doesn't touch `Alert`, `User`, or the channel layer.

Everything else from the last pass — `User`, `Alert`, `NotificationAttempt`'s core shape — stays as-is; only the event-storage layer changes.
