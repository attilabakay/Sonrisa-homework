prompt with mvp features and datamodel: Now that we have data models and the MVP, we just need to identify the key workflows of the app, like ingestion and such.
The system centers on four primary workflows to execute the core loop: data ingestion, user alert configuration, event matching, and notification dispatch.

# World Event Alerts System — Key Workflows

Derived from `mvp_final.md` (scope) and `datamodel_final.md` (schema). Five workflows
cover the full loop; two are admin-only setup flows.

---

## 1. User Auth & Account

**Actors:** User, Admin

1. User registers with email + password → `User` row created (`isActive = true` by default, `isAdmin = false`).
2. User logs in → basic auth check against `User.pw`.
3. Admin can flip `User.isActive` to disable an account.
4. Gate check: any alert-creation or ingestion-triggered notification for a `user` whose `isActive = false` should be skipped at the matching/dispatch step (not just login) — otherwise a disabled user could still get notified on pre-existing alerts. Worth deciding explicitly since the schema doesn't enforce it on its own.

**Deferred:** SSO/OAuth, self-serve password reset, roles beyond enabled/disabled.

---

## 2. Admin: Data Source Onboarding (mapping UI)

**Actors:** Admin

This is what makes "add a second provider in an existing category" a config change.

1. Admin selects a target `DataType` (NEWS / MARKET / DISASTER / WEATHER-stretch) — i.e. which fixed interface this source will feed.
2. Admin enters provider connection info: `link` (base URL/endpoint), `apikey`.
3. Admin maps the provider's raw response fields → the interface's fixed fields (e.g. provider's `price` → `MarketInterface.value`, provider's `hood` → `EmergencyInterface.region`). Stored as `fieldMapping` JSON.
4. `DataSource` row is saved. It's now eligible to be polled (Workflow 3).

**Note:** this flow only handles a *second provider within an existing category*. A genuinely new category (beyond News/Market/Emergency/Weather) still needs new interface code — out of scope for this UI by design.

---

## 3. Ingestion (polling)

**Actors:** System (scheduled job)

1. For each active `DataSource`, poll `link` on an interval (polling, not streaming, per MVP scope).
2. Raw response received.
3. Store it as-is: new `dataEntry` row — `type` (copied from `DataSource.type`), `dataSourceId`, `rawJsonData`, `receivedAt`. Raw storage first, mapping applied at read-time — keeps ingestion simple and re-mappable later if `fieldMapping` changes.
4. New `dataEntry` triggers (or queues for) the Matching workflow.

**Open question worth pinning down before build:** polling cadence per source type — same interval for all three, or type-specific (e.g. disaster feed polled more frequently than a market feed)? Not specified in either source doc.

**Deferred:** real-time streaming, multi-source dedup within a category.

---

## 4. Matching

**Actors:** System

Runs once per new `dataEntry`.

1. Build the typed in-memory interface object from `dataEntry.rawJsonData` + the owning `DataSource.fieldMapping` (e.g. a `MarketInterface` instance with `ticker`, `value`, `timestamp`).
2. Fetch all `Alert` rows where `Alert.type == dataEntry.type` and `Alert.isActive == true`.
3. For each candidate alert, evaluate `Alert.criteria` against the interface object using the type-specific trigger logic:
   - **Keyword:** contains/equals match against event text.
   - **Market:** threshold or % move — comparator logic (absolute vs. relative, and against what: last entry? a stored baseline?) is provider-dependent and explicitly not pinned down yet in the MVP doc. This workflow needs that decision before matching can be implemented, since "value change" implies comparing against *some* prior state, and `dataEntry` alone doesn't define what that prior state is (see note below).
   - **Location:** named-region equality match.
4. Each match → hand off to Notification Dispatch (Workflow 5) with `(alert, dataEntry)`.

**Design gap to flag:** the schema has no explicit "previous value" for market alerts — presumably the matcher queries the prior `dataEntry` for the same `ticker`/source at evaluation time rather than storing state on `Alert`. Worth confirming that's the intended approach, since it's the same open question §6 of the MVP doc flags for the future generic field engine, just showing up early here too.

---

## 5. Notification Dispatch

**Actors:** System

Runs once per (alert, dataEntry) match from Workflow 4.

1. Look up `Alert.sender` → `Sender` row (has `type`: EMAIL/SLACK, and `config`: address/webhook/token).
2. Select the matching `NotificationChannel` adapter (Email or Slack) based on `Sender.type`.
3. Build the payload (plain message — no per-channel templating in MVP) and call `adapter.send(payload)`.
4. Record the outcome as a `NotificationAttempt`: `alertId`, `dataEntryId`, `status` (SENT/FAILED), `error` (if failed), `sentAt`.
5. Status reflects call-success only — not delivery or read confirmation.

**Deferred:** retry/backoff on failure, rich per-channel formatting, delivery/read confirmation.

---

## 6. Alert Lifecycle (user-facing)

**Actors:** User

1. **Create:** user picks a type (keyword/market/location), fills type-specific `criteria`, and picks/creates a `Sender` (channel + config) → new `Alert` row, `isActive = true`.
2. **View active:** list `Alert` rows for the logged-in user where `isActive = true`.
3. **Delete:** remove the `Alert` row (or set `isActive = false` — schema supports either; soft-delete via `isActive` is cheaper and keeps `NotificationAttempt` FK history intact, so probably preferable even though the MVP doc phrases it as "delete + recreate").
4. **Edit:** not supported — delete + recreate, per MVP scope.

---

## 7. Admin Monitoring

**Actors:** Admin

1. **Event log view:** list recent `dataEntry` rows per `DataSource`, for debugging/visibility (not analytics).
2. **User list:** all `User` rows with enable/disable toggle, and a view into which `Alert`s belong to which user (join on `Alert.user`).

---

## Workflow Dependency Order

For build sequencing: **1 (auth) → 2 (source onboarding) → 3 (ingestion) → 4 (matching) → 5 (dispatch)**, with 6 (alert CRUD) and 7 (admin monitoring) able to be built in parallel once the schema exists, since they mostly touch `Alert`/`User`/`dataEntry` directly rather than depending on the pipeline being live.