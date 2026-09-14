## World Event Alerts System — Workflows (entity CRUD framing)

**1. User (admin-facing)**
- Create: self-registration (email + password) → `isActive = true`, `isAdmin = false`
- Read: admin "get all users" view, joined to their `Alert`s
- Update: none in MVP (no edit, no self-serve password reset)
- Deactivate: admin flips `isActive → false` — must be enforced at matching/dispatch time, not just login, so a disabled user's existing alerts stop firing
- Delete: not supported (would orphan `Sender`/`Alert`/`NotificationAttempt` FKs)

**2. DataSource (admin-facing)**
- Create: admin selects `DataType`, sets `link`/`apikey`, defines `fieldMapping` → row saved, `isActive = true` by default
- Read: admin view of configured sources, paired with the event-log (`dataEntry`) view per source
- Update: `link`, `apikey`, `fieldMapping` all editable — this is the mechanism that makes "new provider, same category" a config change
- Deactivate: admin flips `isActive → false` — ingestion job must filter on this at poll time so a paused source actually stops polling
- Delete: only safe for a source with no `dataEntry` history; otherwise deactivate is the real lifecycle op

**3. dataEntry (system-only, ingestion)**
- Create: scheduled job polls each `isActive` `DataSource.link`, stores raw response as-is (`type`, `dataSourceId`, `rawJsonData`, `receivedAt`) — creation triggers Matching
- Read: admin event-log view, recent entries per source, debugging only
- Update: none — immutable by design, so `fieldMapping` changes stay re-interpretable at read-time
- Deactivate: n/a — it's a log record, not a toggleable rule
- Delete: unaddressed/out of scope — would break `NotificationAttempt.dataEntryId` FK history if ever needed

**4. Alert (user-facing)**
- Create: user picks type, sets `criteria`, picks/creates `Sender` → row saved, `isActive = true`
- Read: user's "view active" list (`isActive = true`, scoped to logged-in user)
- Update: not supported — delete + recreate is the MVP's answer to editing
- Deactivate: the actual implementation of the MVP's "delete" — flips `isActive → false`, preserves `NotificationAttempt` FK history
- Delete: possible but not preferred, given it discards audit history that deactivate keeps

**5. NotificationAttempt (system-only, audit trail)**
- Create: one row per (alert, dataEntry) match, after the channel adapter's `send()` call — records `status` (SENT/FAILED), `error`, `sentAt`
- Read: admin "get all" view, system-wide, joined through `Alert → User` for context — the debugging surface for "did this fire, did it succeed"
- Update: none — append-only, nothing to mutate post-send
- Deactivate: n/a
- Delete: none — undermines the audit purpose

---

**Build order implied by this framing:** User → DataSource → dataEntry (depends on DataSource) → Alert (depends on User + Sender, independent of DataSource/dataEntry) → NotificationAttempt (depends on Alert + dataEntry both existing). Alert CRUD can be built in parallel with DataSource/dataEntry since it doesn't depend on the ingestion pipeline being live — only the Matching step (dataEntry × Alert) and the NotificationAttempt write need both sides done.