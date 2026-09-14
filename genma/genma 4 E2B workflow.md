prompt with mvp features and datamodel: Now that we have data models and the MVP, we just need to identify the key workflows of the app, like ingestion and such.
The system centers on four primary workflows to execute the core loop: data ingestion, user alert configuration, event matching, and notification dispatch.

Based on the scope defined in `mvp final.md` and the data model in `datamodel final.md`, the key workflows for the World Event Alerts System MVP can be broken down into three primary, interconnected processes: **Data Ingestion**, **Alert Matching**, and **Notification Dispatch**.

Here are the identified key workflows:

---

### Workflow 1: Data Ingestion (The Inflow)

This workflow handles the raw data coming from external providers and transforms it into a usable format for the system.

1.  **Receive Raw Data:** An external data source (News, Market, Emergency) pushes a payload to the system.
2.  **Determine Type:** The system identifies the `DataType` of the incoming data (e.g., `NEWS`, `MARKET`, `DISASTER`).
3.  **Map & Store:** The raw JSON data is processed using the configured `DataSource.fieldMapping` to align with the specific `DataType` interface expectations.
4.  **Create Entry:** A new `dataEntry` record is created, linking the raw data to the specific `DataSource`.

> **Key Artifacts:** `DataSource`, `dataEntry`

### Workflow 2: Alert Matching (The Engine)

This workflow determines if the newly ingested data warrants a user notification based on existing rules.

1.  **Query Active Alerts:** The system queries the list of active `Alert` records.
2.  **Apply Trigger Logic:** The incoming `dataEntry` is evaluated against the `criteria` field of each relevant `Alert`.
    *   *Example:* If the `dataEntry.type` is `MARKET`, the system checks if the `dataEntry.rawJsonData` matches the `Alert.criteria` (e.g., checking for a specific `ticker` and a value change condition).
3.  **Alert Firing:** If a match is found, a new `Alert` record is created (or an existing one is updated/activated).

> **Key Artifacts:** `Alert`, `dataEntry`, `DataType`

### Workflow 3: Notification Dispatch (The Output)

This workflow executes the required action when an alert is successfully triggered.

1.  **Identify Recipients:** The triggered `Alert` object is inspected to find the designated `user` and the required `sender` (Notification Channel).
2.  **Prepare Payload:** A payload is constructed, typically derived from the `Alert` details and the relevant `dataEntry`.
3.  **Dispatch Notification:** The system calls the appropriate implementation of the **Channel Adapter interface** (`NotificationChannel.send(payload)`).
4.  **Record Attempt:** A `NotificationAttempt` record is created to track the outcome, status (`SENT` or `FAILED`), and any associated error message.

> **Key Artifacts:** `Alert`, `Sender`, `NotificationAttempt`, `NotificationChannel` interface

---

### Summary of the Core Loop

The MVP proves the functionality by successfully completing this loop:

$$\text{External Event} \xrightarrow{\text{Workflow 1: Ingestion}} \text{dataEntry} \xrightarrow{\text{Workflow 2: Matching}} \text{Alert} \xrightarrow{\text{Workflow 3: Dispatch}} \text{NotificationAttempt}$$