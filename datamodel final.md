Here's the consolidated schema incorporating everything from the discussion — the shared `DataType` enum, the `NotificationAttempt` addition for §3's delivery tracking, and `isActive` on Alert:

## DataType (shared enum)
```
DataType: NEWS | MARKET | DISASTER   (+ WEATHER if the stretch goal happens)
```
Used consistently across DataSource, dataEntry, and Alert so the three stay linkable without string-matching guesswork.

## User
```
User {
  id
  email
  pw
  isAdmin
  isActive
}
```

## Sender
```
Sender {
  id
  type            // EMAIL | SLACK
  user            // FK → User
  config          // json/text — address, webhook URL, token, etc.
}
```

## DataSource
```
DataSource {
  id
  type            // DataType
  link            // provider base URL / API endpoint
  apikey
  fieldMapping    // json — provider's raw field names → interface field names
}
```

## dataEntry
```
dataEntry {
  id
  type            // DataType
  dataSourceId    // FK → DataSource
  rawJsonData
  receivedAt
}
```

## Alert
```
Alert {
  id
  type            // DataType
  criteria        // json — keyword / ticker+threshold+comparator / region, depending on type
  user            // FK → User
  sender          // FK → Sender
  isActive
}
```

## NotificationAttempt
```
NotificationAttempt {
  id
  alertId         // FK → Alert
  dataEntryId     // FK → dataEntry (the entry that triggered this send)
  status          // SENT | FAILED
  error           // nullable
  sentAt
}
```

Six entities total. `NewsInterface`/`MarketInterface`/`EmergencyInterface` stay as in-memory-only constructs (built at match time from `dataEntry.rawJsonData` + `DataSource.fieldMapping`) — they don't need their own row here since they're not persisted.