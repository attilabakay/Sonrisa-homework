# World Event Alerts System — MVP Scope

**Constraint:** 24-hour build window, ~20.5 hours remaining at time of writing.
**Goal:** ship the smallest version that proves the core loop — user defines a rule,
a real-world event matches it, they get notified — without boxing out future
extensibility on either the data-source side or the notification-channel side.

---

## 1. Users

- Register / login (basic auth — email + password is enough, no SSO/OAuth for the user side)
- Subscribe to topics = create an alert (see §2)
- Define delivery channel per alert (email, Slack — see §3)
- Admin can enable/disable a user account
- **Deferred:** roles beyond enabled/disabled, rate limiting per user, password reset flow (use a manual/admin reset if needed for demo purposes)

---

## 2. Alerts

Three alert types, each with its own trigger logic. No generic field engine in the
MVP (see §6 — this is intentionally deferred, not forgotten).

| Type | Trigger logic | Notes |
|---|---|---|
| **Generic / keyword** | Alert fires when incoming event text matches a keyword | Simple contains/equals match, no NLP/fuzzy matching |
| **Market value** | Alert fires on value change (threshold or % move) | Exact comparison logic (absolute vs. relative) depends on what the chosen market API actually returns — decide at implementation time, not now |
| **Location / disaster / weather** | Alert fires when an event matches a region | Named-region match is enough for MVP; radius-based matching is a stretch goal if time allows |

- Basic lifecycle: create, delete, view active alerts
- **Deferred:** edit (delete + recreate for now), user-facing alert history, digest/quiet-hours, dedup across sources

---

## 3. Notification Channels

A single **Channel Adapter interface** — not hardcoded per-channel logic:

```
NotificationChannel.send(payload) → { status: "sent" | "failed", error?: string }
```

- Two adapters implemented: **Email**, **Slack**
- Delivery status = **call-success only** — did the API call to the provider
  succeed or fail. This is *not* delivery/read confirmation (that's a much harder,
  provider-inconsistent problem) — just enough to catch "SMTP auth failed" /
  "Slack token expired" type errors.
- Adding a third channel later = write one new adapter, no changes to alert
  config, matching engine, or data model.
- **Deferred:** rich delivery confirmation, per-channel formatting/templates beyond a plain message, retry/backoff logic

---

## 4. Data Sources

Three typed source interfaces (a fourth — weather — if time allows):

- `NewsInterface`
- `MarketInterface`
- `EmergencyInterface`
- `WeatherInterface` *(stretch goal)*

Each interface defines the fixed set of fields the matching engine understands
for that category (e.g. `MarketInterface` exposes `ticker`, `value`, `timestamp`;
`EmergencyInterface` exposes `region`, `eventType`, `severity`, `timestamp`).

**One data source per category to start** (one news API, one market feed, one
disaster/emergency feed). Adding a *second provider within an existing category*
is a mapping exercise (see §5), not new code. Adding a genuinely new category
still requires a new interface — that's an accepted MVP boundary, not an oversight.

- **Deferred:** multi-source aggregation within a category, deduplication across sources, real-time streaming (polling is fine for MVP)

---

## 5. Admin View

- **Data source event log** — local list of recent events received per source, for debugging/visibility (not a full analytics dashboard)
- **Add data source via mapping UI** — admin selects a target interface (News / Market / Emergency), then maps that provider's response fields to the interface's fields. This is what makes adding a second provider in an existing category a config change, not a deploy.
- User list with enable/disable + visibility into which alerts belong to which user
- **Deferred:** audit log, source health/latency monitoring, rate-limit management, role management beyond enabled/disabled

---

## 6. Explicitly Deferred (Post-MVP)

Noting these so they're deferred on purpose, not dropped by accident:

- **Universal template engine** — replacing the fixed typed interfaces (§4) with
  a dynamic field system (text / number / location / date fields, each with
  typed filter operators) so a brand-new category can be added via config alone,
  with no new interface code. This is a real architectural upgrade path, not a
  rejected idea — it's out of scope only because of the 24-hour constraint. If
  revisited, the main open design question is how a "value-change" rule (which
  needs a previous-value comparison, not just a snapshot field) fits into a
  generic field model — flagged during scoping, unresolved, worth settling
  before building the template engine.
- Delivery/read confirmation beyond call-success status
- User-facing alert history
- Roles/permissions beyond enabled/disabled; per-user rate limiting
- Additional channel adapters (Discord, SMS, Teams, etc.) and additional source providers per category
- Dedup, digesting, quiet hours, geographic radius matching

---

## What This MVP Proves

1. The channel adapter abstraction works with two real implementations (email, Slack).
2. The source interface + mapping pattern works for adding a provider within a category without new code.
3. The end-to-end loop — ingest → match → notify — functions for three distinct alert types.

Everything in §6 is additive on top of this core; none of it should require re-architecting what's built here, with the one caveat noted above about value-change logic if the template engine is built later.