prompt with mvp features and datamodel: Now that we have data models and the MVP, we just need to identify the key workflows of the app, like ingestion and such.
The system centers on four primary workflows to execute the core loop: data ingestion, user alert configuration, event matching, and notification dispatch.

**1. Data Ingestion & Normalization**

* **Trigger:** Scheduled cron job / polling interval per `DataSource`.
* **Process:**
1. Fetch raw payload from the provider base URL/endpoint using `DataSource.link` and `apikey`.


2. Persist the raw payload into a new `dataEntry` record (`rawJsonData`, `type`, `receivedAt`, `dataSourceId`).


3. Transform `rawJsonData` using `DataSource.fieldMapping` into its corresponding in-memory construct (`NewsInterface`, `MarketInterface`, or `EmergencyInterface`).




* **Output:** Normalized in-memory object passed directly to the Matching Engine.

**2. Alert Configuration (User Setup)**

* **Trigger:** User creates a new alert via the UI.
* **Process:**
1. User selects a `DataType` (NEWS, MARKET, DISASTER).


2. User inputs specific `criteria` (e.g., keyword for News, threshold/ticker for Market, region for Disaster).


3. User selects or configures a delivery `Sender` (Email address or Slack webhook).


4. System creates an `Alert` record attached to the `User` and `Sender` with `isActive = true`.





**3. Event Matching Engine**

* **Trigger:** A new `dataEntry` is ingested and normalized.
* **Process:**
1. Query active `Alert` records matching the incoming `dataEntry.type`.


2. Evaluate criteria against the normalized memory interface:


* **NEWS:** String contains/equals match against title/body.


* **MARKET:** Numeric comparator (threshold / % move) against current value.


* **DISASTER:** Location string match against region.




3. For every match, instantiate a dispatch payload containing the alert details and context.



**4. Notification Dispatch**

* **Trigger:** The Matching Engine identifies a match.
* **Process:**
1. Retrieve the associated `Sender` configuration.


2. Pass payload to the generic `NotificationChannel` interface (routing to Email or Slack adapter).


3. Execute provider API call.


4. Log the result in a `NotificationAttempt` record linking `alertId`, `dataEntryId`, `status` (`SENT`/`FAILED`), timestamp, and any error message.





**5. Admin Data Source Mapping (Setup Workflow)**

* **Trigger:** Admin connects a new provider to an existing `DataType` category.


* **Process:**
1. Admin creates a `DataSource` entry.


2. Admin maps provider-specific JSON keys to interface keys via the UI to populate `DataSource.fieldMapping`.


3. New ingestion runs automatically parse into the established interface without code changes.





Would you like to refine the exact polling cadence/queue strategy for ingestion, or draft the API routes to support these flows?