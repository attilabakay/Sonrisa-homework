// Alert.criteria is a raw JSON string shaped per type (mvp final.md §2) — shared between
// AlertsPanel (your own alerts) and UsersPanel (admin viewing another user's alerts).
export function describeCriteria(type, criteriaJson) {
  try {
    const c = JSON.parse(criteriaJson)
    if (type === 'NEWS') return `keyword: "${c.keyword}"`
    if (type === 'MARKET') return `${c.ticker} ${c.comparator} ${c.threshold}`
    return `region: "${c.region}"`
  } catch {
    return criteriaJson
  }
}
