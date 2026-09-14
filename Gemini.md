prompt: I have the following task:
"We want users to be able to set up alerts so they get notified when something important happens in the world — like breaking news, market movements, natural disasters, that kind of thing. Should work for both email and Slack. Make it flexible enough that we can add more channels later. We need an admin view too."
I don't want to just jump into it I want to first identify the key features that would be required for this project. 

---

# Key Features & Requirements Specification: Real-Time Multi-Channel Alert System

## 1. Executive Summary & Architectural Goals

The objective of this project is to build an enterprise-grade, highly scalable, and extensible real-time alert platform. The system empowers end-users to create granular monitoring rules for global events—such as financial market moves, breaking news, natural disasters, and regulatory updates—and receive timely notifications across their preferred communication channels (starting with Email and Slack).

The platform is designed with a pluggable architecture to ensure that new notification channels (e.g., Microsoft Teams, SMS, WhatsApp, Webhooks) and new data sources can be integrated seamlessly without altering core rule evaluation or ingestion logic.

---

## 2. Core System Pillars & Detailed Requirements

### Pillar 1: Event Ingestion & Data Source Abstraction

To process diverse data types uniformly, the platform decouples data acquisition from event processing through a standardized Event Normalization Layer.

* **Data Source Connectors:**
  * **Financial Feeds:** Connectors for real-time market data (stocks, FX, crypto, commodities) via WebSockets and REST APIs.
  * **News & RSS Feeds:** Ingestion engines for major media RSS/Atom feeds and breaking news APIs.
  * **Disaster & Emergency Feeds:** Integrations with weather services (NOAA), seismic monitoring (USGS), and global crisis feeds.
  * **Generic Ingestion Endpoint:** Standardized REST/Webhook endpoint for third-party systems to push events directly into the pipeline.
* **Unified Event Payload Schema:**
  * Every ingested event is transformed into an immutable internal schema containing:
    * `event_id`: Unique identifier (UUID).
    * `source_id`: Source identifier and feed origin.
    * `category`: Broad taxonomy (e.g., `FINANCE`, `DISASTER`, `NEWS`, `REGULATORY`).
    * `severity`: Normalized level (`INFO`, `WARNING`, `CRITICAL`, `EMERGENCY`).
    * `timestamp`: ISO-8601 UTC timestamp.
    * `title` & `body`: Human-readable event summary and detailed payload.
    * `metadata`: Structured key-value map (e.g., ticker symbols, percentage change, geographic coordinates).
* **Ingestion Resilience:**
  * Support for both push-based (WebSockets, Webhooks) and pull-based (configurable polling intervals) data fetching.
  * Dead Letter Queue (DLQ) for malformed incoming payloads to prevent pipeline blockages.

---

### Pillar 2: Alert Rule Engine & Rate Control

The Rule Engine evaluates normalized incoming events against millions of user-defined alert conditions with minimal latency.

* **Flexible Rule Definitions:**
  * **Keyword & Pattern Matching:** Exact matches, wildcard searches, and regex patterns within event titles/bodies.
  * **Categorical Filtering:** Filtering by topic, geographic region, or severity level.
  * **Numerical Thresholds:** Triggers based on metrics (e.g., `SPY movement > 2.5% in 15 mins`, `Magnitude > 6.0 Earthquake`).
  * **Composite Logic:** Boolean combinations (`AND`, `OR`, `NOT`) allowing multi-variable alerts (e.g., `Category == FINANCE AND Ticker == AAPL AND Severity >= WARNING`).
* **Evaluation Pipeline:**
  * High-throughput stream processing engine utilizing in-memory indexing or streaming criteria logic for sub-second rule matching.
* **Deduplication, Cooldown & Anti-Storm Controls:**
  * **Cooldown Windows:** User-configurable or system-enforced quiet periods per rule (e.g., notify at most once every 15 minutes per matching topic).
  * **Flapping Prevention:** Automatic suppression when an event repeatedly toggles across a threshold in a short window.
  * **Alert Grouping / Batching:** Option to rollup high-frequency events into a single digest (e.g., hourly/daily summaries) during major breaking incidents.

---

### Pillar 3: Pluggable Notification Delivery Pipeline

The delivery engine isolates channel delivery implementations from business rules using an extensible provider pattern.

* **Provider Abstraction Layer (Adapter Pattern):**
  * Core interfaces (`NotificationChannel`, `MessageFormatter`) define contract methods (`send()`, `validate_credentials()`, `format_payload()`).
  * Adding a new channel (e.g., Microsoft Teams, Twilio SMS) requires only implementing a new adapter without touching core routing code.
* **Initial Delivery Adapters:**
  * **Email Channel:**
    * Support for transactional providers (SendGrid, Postmark, AWS SES, or SMTP).
    * Rich HTML email templates with plain-text fallbacks.
    * Inlined deep links to user dashboard and source articles.
  * **Slack Channel:**
    * Webhook URL integration for custom channels.
    * Slack App OAuth integration for sending direct messages or enterprise channel posts.
    * Rich Block Kit formatting with interactive buttons (e.g., "Acknowledge", "View Source", "Mute 1 Hour").
* **Execution & Resilience Infrastructure:**
  * Asynchronous background job queues (e.g., Celery, BullMQ, Redis/RabbitMQ) for non-blocking message processing.
  * Exponential backoff with jitter for retrying failed deliveries due to rate limits or transient provider downtime.
  * Idempotency guarantees using unique delivery keys to prevent duplicate user notifications.

---

### Pillar 4: User Self-Service Portal

An intuitive web interface allows users to configure alerts and manage notification preferences without administrative intervention.

* **Alert Builder & Management:**
  * Step-by-step wizard for defining alert rules, threshold conditions, and target channels.
  * Single-click toggle to enable, pause, or delete alerts.
  * Channel routing rules per alert (e.g., Critical alerts to Slack + Email; Info alerts to Email only).
* **Destination & Identity Setup:**
  * **Email Management:** Email address verification and primary/secondary address configuration.
  * **Slack Connection:** One-click OAuth "Add to Slack" flow and webhook manager.
* **Personal Notification History & Audit:**
  * Searchable log of sent alerts, including timestamp, trigger payload, matched rule, and delivery status (Delivered, Pending, Failed).
* **Preferences & Schedules:**
  * Timezone settings and global "Do Not Disturb" (DND) / quiet hours.
  * Summary/digest preferences (Instant vs Daily Digest).

---

### Pillar 5: Administration & System Control View

A centralized admin control center gives operations teams complete visibility and control over the platform's health and usage.

* **Delivery & Queue Health Monitoring:**
  * Real-time metrics on message queues (pending jobs, processing rates, failure percentages).
  * Per-channel status dashboards monitoring SendGrid, Slack API, and SMTP health/latencies.
  * Dead Letter Queue management for viewing and re-queueing failed deliveries.
* **Data Source Administration:**
  * Active status, ping latency, and error rate tracking for all external data connectors.
  * Controls to adjust polling frequencies or pause specific feeds during upstream maintenance.
* **Manual Overrides & System Broadcasts:**
  * Ability to send manual system-wide broadcast alerts for urgent maintenance or platform announcements.
  * Test suite to send synthetic events to test user rules and channel delivery pathways.
* **User & Usage Governance:**
  * User list with rate limit tiering (e.g., Free vs Premium alert allocations).
  * System audit log recording admin actions, rule creations, and security events.

---

## 3. End-to-End Data Flow Architecture

```
[ External Data Sources ] (Financial APIs, News RSS, Weather Services)
           │
           ▼
[ Ingestion & Normalization ] ──► Schema Validation & DLQ
           │
           ▼
[ Rule Evaluation Engine ]  ──► Matches against stored user rules
           │
           ▼
[ Deduplication & Rate Control ] ──► Enforces cooldowns & grouping
           │
           ▼
[ Async Task Queue ] (Redis / RabbitMQ)
           │
           ▼
[ Pluggable Provider Layer ]
 ├── Email Adapter (SendGrid / SES) ──► End User Email
 ├── Slack Adapter (Webhooks / App) ──► End User Slack Channel
 └── [Future Channels: Teams, SMS, Webhooks]
           │
           ▼
[ Delivery Analytics & Audit Log ] ──► Admin & User Dashboards
```

---

## 4. Key Non-Functional Requirements (NFRs)

* **Performance & Latency:** Ingestion-to-delivery latency < 3 seconds for critical alerts.
* **Extensibility:** Standardized plugin framework permitting new channel adapters to be added in < 1 day of development.
* **Scalability:** Horizontal auto-scaling of worker nodes during major breaking news spikes.
* **Reliability:** 99.9% uptime target with zero data loss on matching rules during source outages.
* **Security:** AES-256 encryption at rest for user API keys, webhooks, and Slack tokens; TLS 1.3 in transit.

---

## 5. Next Steps & Implementation Phasing

1. **Phase 1 (MVP):** Event normalization schema, keyword/category matching engine, Email + Slack Webhook integrations, basic User Portal.
2. **Phase 2 (Admin & Reliability):** Admin queue monitoring dashboard, rate-limiting/cooldown engine, Slack OAuth app integration, Delivery Audit Logs.
3. **Phase 3 (Expansion):** Advanced composite rule logic, Microsoft Teams & Webhook channels, user quiet hours, automated feed health checks.