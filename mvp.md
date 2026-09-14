---
claude promt with atteched md files: I'm working on a project based on this brife: "We want users to be able to set up alerts so they get notified when something important happens in the world — like breaking news, market movements, natural disasters, that kind of thing. Should work for both email and Slack. Make it flexible enough that we can add more channels later. We need an admin view too."  As the brife is quite broad I decided to run it by multiple AI angents these md files are the result of this compare the 4 MVPs for the project and display them.
---

Here's a side-by-side breakdown of the four MVP proposals:

## Quick take on each

**Genma 4 (E2B)** — Frames the MVP as an "architectural proof": the point isn't the alert itself, it's proving the pluggable notification system works end-to-end with Email + Slack. Includes basic admin (users/roles, single-source config) but defers the oversight dashboard. Solid, but light on ingestion detail (just "one reliable source") and doesn't address dedup/noise at all.

**DeepSeek Coder V2** — The broadest and shallowest. Adds things the brief never asked for (user registration/auth as a headline feature, feedback/support system, formal QA process) while barely touching the thing that was explicitly called out as hard: the pluggable channel architecture. It says "flexible to add channels" but never describes *how* — no interface/adapter concept. Reads more like a generic SaaS MVP checklist than a response to this specific brief.

**Sonnet 5** — Most disciplined scope-cutting. Explicitly separates what's "deferred" from what's "in scope but must be built correctly now" — notably calling out the channel abstraction as non-negotiable architecture, not a nice-to-have. Also the only one that names the two riskiest assumptions the MVP should test (does the abstraction survive a 2nd channel; can events be matched fast enough). Trims aggressively elsewhere (no edit, no dedup, polling ok) which keeps it lean but is upfront about every cut.

**Gemini** — Not really an MVP — it's a phased full system design. Phase 1 alone (event normalization schema, matching engine, Email + Slack, "basic portal") is bigger than any other model's entire MVP: rich HTML templates, Block Kit with interactive buttons, DLQ, async queues with backoff, encryption at rest. Technically excellent as a target architecture, but if you built exactly "Phase 1" as written it'd take multiple sprints, not a first pass.

## Comparison table

| Dimension | Genma | DeepSeek | Sonnet 5 | Gemini |
|---|---|---|---|---|
| **MVP philosophy** | Prove the plugin mechanism works | Ship broad basic features | Prove the two riskiest assumptions | Phase 1 of full architecture |
| **Channel abstraction called out explicitly** | Yes (API-first, registry) | No — just "integrate email + Slack" | Yes — treated as the core deliverable | Yes — full adapter pattern w/ interfaces |
| **Alert config scope** | Name/source/trigger/recipient | Category selection only | Fixed taxonomy, on/off filters, 1 channel/alert | Wizard w/ thresholds + channel routing rules |
| **Data sources in MVP** | 1 source | Unspecified | 1 source *per category* | Full connector suite (finance/news/disaster) even in Phase 1 |
| **Dedup / noise control** | Not mentioned | Not mentioned | Explicitly deferred (named as accepted risk) | Included in Phase 1 (cooldowns, anti-storm) |
| **Admin view in MVP** | Users/roles + single-source config | "Basic admin functionalities" (vague) | Alert search + pipeline status only | Full queue/DLQ monitoring, source admin, broadcasts |
| **Slack integration** | Unspecified depth | Just "Slack API" | OAuth + channel picker | OAuth app + Block Kit w/ interactive buttons |
| **Explicitly deferred items listed** | Yes (table) | No — no deferral list | Yes — deferred per section | Pushed to Phase 2/3 instead of cut |
| **Scope realism for "MVP"** | Reasonable | Reasonable but unfocused | Tightest, most honest tradeoffs | Oversized for an MVP label |

## Bottom line

If the goal is genuinely minimal and shippable, **Sonnet 5's** version is the most rigorous — it's the only one that treats "extensible to future channels" as an architecture decision to get right on day one rather than a feature to add or defer, while still cutting real scope elsewhere (no dedup, no edit, polling is fine). **Genma's** is a reasonable second, similar philosophy but less detailed on tradeoffs. **Gemini's** is the strongest reference architecture if you want to see the full system's shape, but you'd need to cut it down yourself before calling it an MVP. **DeepSeek's** is the weakest match to the brief — it under-engineers the one thing the brief flagged as important (extensibility) while over-engineering things it didn't ask for (auth, support tooling).

If it'd help, I can merge these into a single consolidated MVP spec that takes Sonnet 5's scoping discipline and channel-abstraction design as the backbone, with Gemini's data schema and admin-monitoring ideas folded in as the "Phase 2" target.

---
prompt: My issue with all the MVPs is that it seems to misunderstand "add more channels later"; it could mean adding more data sources, and it could also mean adding more communication channels, such as via discord or via facebook for example.
---

That's a real gap, and it's worth noting none of the four caught it — they all silently resolved the ambiguity toward "communication channels" without flagging that there was a choice being made.

**Re-reading the brief with that in mind:**

> "Should work for both email and Slack. Make it flexible enough that we can add more channels later."

The word "channels" sits directly after "email and Slack," which is why every model anchored on communication channels — Discord, Teams, SMS, WhatsApp, webhooks. But it's just as plausible the stakeholder was thinking about the *first* sentence — "breaking news, market movements, natural disasters" — and meant "more channels" as in more feeds/sources: more news wires, more market data providers, more disaster APIs, maybe even more event categories entirely (regulatory filings, sports, whatever comes up next). "Add more channels later" is genuinely satisfied by either reading, and a stakeholder using casual language likely wasn't distinguishing between them at all — they may have meant both, or not have thought about the distinction.

**How each doc actually handled it, now that I look again:**

| | Treated notification channels as pluggable | Treated data sources as pluggable |
|---|---|---|
| Genma | Yes — explicit plugin/registry | No — "single data source," managed directly, no abstraction |
| DeepSeek | Weakly (just "integrate more later") | No |
| Sonnet 5 | Yes — explicit adapter interface | No — sources are just "one per category," no interface mentioned |
| Gemini | Yes — full adapter pattern | Partially — has a "Generic Ingestion Endpoint" + normalized schema, which is source-agnostic by design, but doesn't call this out as satisfying the "add more channels" requirement |

So Gemini is the only one that *accidentally* built something extensible on the ingestion side too (the normalized event schema + generic endpoint would make adding a new source cheap), but even it doesn't frame that as answering "add more channels later" — it's just good architecture that happens to cover it.

**Why this matters for scoping, not just semantics:**

If "channels" includes data sources, the MVP bar changes. Under every one of these docs' current framing, "prove extensibility" only requires showing a second *notification* channel works (Slack after Email). If sources count too, the real test of extensibility should also include: can you add a second *data source* (say, a second news feed) without touching the matching engine or UI? None of the four MVPs designed their ingestion layer to prove that — they all treat "one data source" as an accepted MVP simplification, not as something needing its own abstraction the way notification channels got one.

**What I'd do:** this is a one-line clarifying question to whoever wrote the brief — "when you say 'more channels,' do you mean more places we notify people (Discord, Teams, SMS) or more places we pull events from (more news/market/disaster feeds), or both?" It's cheap to ask and expensive to guess wrong, since it changes which part of the system needs the plugin architecture on day one.

If you can't get an answer in time, the safer bet is to assume **both** need the same treatment — a generic `NotificationChannel` interface *and* a generic `EventSource` interface — since that's a coherent, symmetric architecture (event in → normalize → match → notify out, pluggable on both ends) rather than picking one arbitrarily. Want me to sketch what an MVP looks like under that dual interpretation?

---
prompt: I  discussed this issue with all the models and updated their response in the md files. Check again and compare them based on the updates.
---

Here's how each model handled the follow-up — and the gap between "acknowledging the ambiguity" and actually fixing the architecture varies a lot more this round.

## What each one actually did

**Genma** — Revised the MVP directly: bumped data ingestion from "one source" to "at least two distinct sources," and admin scope now covers "both" sources. But it stopped there — there's no source *adapter interface* introduced. Communication channels still get the "API-first design / pluggable service" treatment, but data sources just get "pull from more of them." So it responded to the letter of your feedback (more sources in scope) without applying the same architectural pattern it used for channels. That's an asymmetry your original critique was specifically about.

**DeepSeek** — Didn't touch the MVP at all. It agreed you were right, then bucketed Discord, Facebook, a mobile app, more data sources, and web scraping into a generic "Future Enhancements" list — no interface, no adapter concept, nothing added to MVP scope. This is the same pattern as its first response: acknowledge in prose, don't change the architecture. Weakest of the four again.

**Sonnet 5** — The only one that stopped and asked before rewriting: it flagged that fixing this properly means adding a second abstraction point, and asked whether you wanted it built now or just documented as a design constraint. Once you said "go broad," it added a `Source adapter interface` mirroring the existing channel adapter — same pattern (adapter in → normalized schema out), applied symmetrically to data sources. Updated the "what this proves" section to three validated assumptions instead of two, and added an explicit note at the top of the doc naming the ambiguity so it's documented, not just fixed silently.

**Gemini** — Skipped the clarifying question and went straight to a full symmetric answer: named both abstractions explicitly (`EventIngestionAdapter` for inbound, `NotificationChannelAdapter` for outbound), each with its own interface contract, and folded both into a revised MVP table. Architecturally this lands in the same place as Sonnet 5's — two adapter interfaces, one per direction — just delivered as an immediate answer rather than a check-in first.

## Updated comparison

| Dimension | Genma | DeepSeek | Sonnet 5 | Gemini |
|---|---|---|---|---|
| **Asked before assuming scope** | No | No | Yes — one clarifying question | No |
| **Source-side abstraction added** | No — just "2+ sources," no interface | No — not addressed at all | Yes — explicit adapter interface, symmetric to channel one | Yes — `EventIngestionAdapter`, explicit interface |
| **Channel-side abstraction (carried over)** | Yes (from before) | Weak, unchanged | Yes (from before) | Yes — renamed `NotificationChannelAdapter` |
| **MVP scope actually changed** | Yes — 2 sources now required | No — only a "future work" list added | Yes — new §3 in-scope/implemented split | Yes — table updated with both layers |
| **Documents the ambiguity itself** | No | Implicitly, in prose only | Yes — explicit note in the doc | Implicitly, via terminology (Inbound/Outbound) |
| **Symmetry of the two abstractions** | Asymmetric (one gets a pattern, one doesn't) | N/A | Fully symmetric, same pattern named twice | Fully symmetric, distinct interfaces |

## Bottom line

**Sonnet 5** and **Gemini** are now the two serious answers — both correctly recognized that "flexible channels" needs *two* adapter interfaces, not one, and both updated the MVP scope to build the source-side one from day one rather than deferring it. Sonnet 5's edge is process: it asked before committing you to the broader (more expensive) scope, and it left a paper trail in the doc explaining *why* two abstractions exist — useful if someone new picks this up later. Gemini's edge is that its interface names are slightly more precise (`EventIngestionAdapter` / `NotificationChannelAdapter` reads clearer than "channel abstraction" doing double duty).

**Genma** partially responded — it added more sources to scope but never gave sources the same interface treatment it gave channels, so the underlying problem (data-source extensibility bolted on vs. designed in) isn't actually fixed, just deferred less obviously.

**DeepSeek** didn't engage with the architectural question at all — it's the only one of the four still treating this as a feature backlog problem rather than a design problem, three turns into the conversation.

If you want, I can take Sonnet 5's or Gemini's dual-adapter version and turn it into a single reference MVP spec you'd actually hand to an engineering team.

---
prompt: So, based on these documents, I think the minimal viable product should be able to handle:

* users 
   *  register/login
   * subscribe to topics(create alerts)
   * define alert channel(via email vai slack etc)
* alerts
   * create alerts based on keywords for generic events
   * create alerts based on value change for market value
   * create alerts based on locations for natural disaster/whetar warnings
* Data source/channels
   * three main types, each with a connector interface
* Admin view
   * view data channels event log(local list of events)
   * add data source with a mapping feature that translates the datasource response to the internal interface

What do you think about this?
---

This is a tight, sensible MVP — and it fixes the biggest issue we identified: the data-source side now explicitly gets a connector interface, not just "more sources." A few things stand out, one of which I'd flag as worth deciding before you build.

## What's working well

- **Alert types split by category with different trigger logic** (keyword / value-change / location) matches what Sonnet 5 and Gemini converged on — this is the right level of granularity, not over- or under-specified.
- **Connector interface for data sources, named explicitly** — this is exactly the gap Genma and DeepSeek missed and Sonnet 5/Gemini caught. Good that it survived into your own scope.
- **The admin "mapping feature"** is a nice concrete answer to something none of the four docs quite nailed down: instead of only a *code-level* adapter (write a new class per source), you're proposing an admin-configurable mapping UI (source field → internal schema field). That's arguably more MVP-appropriate than a pure code abstraction, since it lets admins add a fourth source without a deploy.

## One asymmetry worth resolving

Data sources get an explicit "connector interface." Notification channels don't — "define alert channel (via email via slack etc)" reads as a feature, not an abstraction. Given that this whole conversation started because "channels" was ambiguous between sources and destinations, I'd make sure the notification side gets the same explicit treatment: a channel adapter interface (payload in → channel-specific send() → delivery status out), even if only email and Slack are implemented behind it. Otherwise you've solved the ambiguity on one side and left it implicit on the other.

## Smaller gaps to decide on, not necessarily build

- **Delivery status** — do users/admins need to know if a notification actually sent (vs. silently failed)? Even a bare `sent`/`failed` flag avoids "I never got the alert" support tickets with no way to diagnose them.
- **Admin user/role management** — your scope has admin managing data sources but not managing users. Was that a deliberate cut, or just not written down?
- **User-facing alert history** — right now only admin sees an event log. Do users see "here's what fired for me," or is that deferred?
- **Threshold specifics for "value change"** — worth pinning down now whether that's absolute (`AAPL > $200`) or relative (`AAPL moves >5%`), since it changes the data model, not just the UI.

None of these need to block you — they're cheap to defer explicitly (the way Sonnet 5's doc did with a "Deferred" list) rather than accidentally.

Want me to write this up as a proper MVP spec doc — your four bullet groups, expanded with the connector/adapter interfaces spelled out symmetrically on both sides, and the deferred items called out explicitly?

---
promt: Delivery status - seems small enough to be included in the MVP, but it might get dropped as it's not a we need to restructure everything kind of thing to include later. Also, my issue with it it's not trivial to conform to all types of delivery status. 
Admin user/role management - this is a hard question; I'm not sure what level of user/role managements should I ilnculde. Later, it would be great to include rate limiters so a user can't intentionally overload our service, but for the MVP I'm not sure anything except a user enabled or disabled is with a way to check which user has which alerts is needed.
 Threshold specifics for "value change" - this should depend entirely on what kind of apis we find and how they serve the data. 
User-facing alert history - This might be a good idea to include but I would but it in the same category as delivery status.
To clarify on the alert channel, I also meant it with a custom interface and connector implementation just forgot to write it into my last response. Also, the data sources should get 3 interfaces like newsInterface, marketInterface, and emergencyInterface, and a weather interface if we have the time. The mapper feature mentioned for the ui should make the trasition between our interface and the data source. So not each data source gets its own interface; each type of data source gets one. This way, it will be open for extension.
---

Good clarifications — a few of these are worth pushing on a little rather than just accepting at face value.

**Delivery status** — Your instinct on riskiness is right (additive, easy to defer), but the "not trivial to conform" problem might be smaller than it looks if you narrow the definition. Email and Slack don't give you the same guarantee: an SMTP handoff succeeding doesn't mean the email landed in an inbox, and a Slack webhook returning 200 doesn't mean a human saw it. If "delivery status" tries to mean *actual delivery confirmation*, you're right that it's genuinely hard to normalize across channels. But if it just means *"did our API call to the channel succeed or fail,"* that's trivial and uniform — every adapter's `send()` either throws/errors or doesn't. That's a much cheaper MVP-safe definition, and it's still useful (catches "Slack token expired," "SMTP auth failed," etc.) without pretending to know whether the user actually saw the message. Worth deciding explicitly which of those two you mean, since the more honest one is nearly free.

**Admin roles** — Enabled/disabled + visibility into which user has which alerts is a reasonable MVP floor. It doesn't box you in either — rate limiting later just needs a counter keyed to the existing user record, no schema rework.

**Threshold specifics** — Agreed, that's an implementation detail that should follow the API's actual data shape, not something to lock in at scoping stage.

**User alert history** — Fine to bucket with delivery status; same shape (nice-to-have, additive, no restructuring risk if dropped).

**On the interface clarification — this is the important one.** What you're describing is different from what I'd been assuming, and it's a better design for an MVP: instead of one adapter per data source, you have a **fixed set of typed interfaces** (news, market, emergency, maybe weather), and each individual source is just a **mapping config** that translates its raw vendor response into whichever typed interface it belongs to. So adding a new market data provider is pure config (map their JSON fields to your market interface's fields) — no new code. That's a real strength: config-level extensibility for the common case of "another provider in a category we already support."

The tradeoff to be explicit about: this buys you cheap extensibility *within* the four categories, but a genuinely new category — say, regulatory filings or sports scores — still needs a fifth interface written in code, since the mapper only ever bridges vendor format → an interface that already exists. That's a perfectly reasonable MVP boundary (you did explicitly scope to three-to-four categories), just worth stating as a deliberate limit rather than discovering it later when someone asks "can we just add a mapping for X" and the answer is no.

Same question applies to the channel side now that you've confirmed it also gets an interface: is that one interface with per-channel adapters (code), or could you eventually want a similar mapper pattern there too — e.g., a generic webhook-based channel where admins map alert fields to a payload template, rather than writing a new adapter class per channel? You don't need to answer that for the MVP, but worth flagging since it's the same pattern question you just resolved on the source side.

Want me to write this up as the finished MVP spec now — your four sections, the typed-interface + mapper architecture spelled out for both sources and channels, delivery status defined as call-success-only, and the deferred list (rich delivery confirmation, user alert history, roles beyond enabled/disabled) stated explicitly?

---
prompt: Let's just assume that we drop the interfaces per category and create a universal template engine where we could translate datasources(api responses) into text fields number fields and location enumerations and such to create filtering based on this templates for the alerts. Instead of hardcoded title, description, and location a dynamic field collection.

---
That's a meaningful step up in flexibility — and it genuinely closes the extension boundary that the typed-interface approach still had (no more "new category needs new code"). But it's worth being honest that this doesn't remove complexity, it relocates it — from "code per category" to "metadata per template." A few things to think through before locking it in.

**What you gain:** a new category (sports scores, regulatory filings, whatever) becomes a config exercise — define a template of typed fields, write a mapping, done. That's the "fully open for extension" version of what we've been circling since the first "add more channels" ambiguity. Good direction.

**Where the complexity actually goes:**

1. **The alert-builder UI has to become metadata-driven too.** Right now "create alert" can have three-ish hardcoded forms (keyword box, ticker+threshold, region picker). With dynamic fields, the UI needs to read a template's field list and render the right control per type — text field → contains/equals input, number field → threshold input, location field → region/radius picker. That's not a big lift once built, but it's real work that the fixed-interface version didn't need.

2. **Filter operators need to be typed, not just fields.** A text field wants "contains/equals," a number field wants "gt/lt/between," a location field wants "within radius/matches region." So the template needs to declare, per field, which operators are valid — otherwise your matching engine doesn't know what a rule against that field means.

3. **"Value change" doesn't actually fit as a plain number field, and this is the one I'd resolve before building.** A number field like "current AAPL price = 201.40" is a snapshot. "Alert when it moves >5%" requires comparing against a *previous* value — that's state and time, not just a typed field. So either: (a) the template engine needs a special field behavior for deltas (store last-seen value, compute change on each update), or (b) that logic lives outside the generic field system entirely, as a small category-specific exception. Worth deciding now, since it's the one place your "generic fields" idea and your original "value change" alert type don't cleanly line up.

4. **Field type taxonomy is now a real design decision.** Text/number/location covers most of what you listed — but do you need date/time as its own type (for "within the last N hours")? And does a location field need to support both "point + radius" and "named region," since disasters/weather might come as either?

None of this is a reason not to do it — it's a stronger architecture than fixed interfaces and probably worth the extra UI work given you explicitly want this open-ended. But I'd nail down the delta/value-change handling specifically before writing the spec, since it's the one place a "generic typed field" doesn't map onto what you actually described wanting.

Want me to draft the field-type taxonomy (text/number/location/date, their valid operators, and how the value-change case gets handled) as the next piece, before we roll it into the full MVP doc?

---
prompt: Let's add that I have only 24 hours for this task, and we have already spent three and a half of it. Okay, let's write the MVP with interface types/data source types, but mention in the MVP that later an additional template engine could be introduced to replace the type interfaces.
---

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