import { useState } from 'react'

// Admin event-log view: recent events received per source, for debugging/visibility
// (mvp final.md §5) — not a full analytics dashboard.
export default function DataEntriesPanel({ dataSources, entries, loading, error, sourceFilter, onFilterChange }) {
  const [expanded, setExpanded] = useState(null)

  return (
    <div className="panel">
      <h2>Data Entries</h2>

      <label>
        Source:{' '}
        <select value={sourceFilter} onChange={(e) => onFilterChange(e.target.value)}>
          <option value="">All sources</option>
          {dataSources?.map((ds) => (
            <option key={ds.id} value={ds.id}>
              {ds.type} — {ds.link}
            </option>
          ))}
        </select>
      </label>

      {loading && <p>Loading entries...</p>}
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Type</th>
            <th>Received at</th>
            <th>Raw data</th>
          </tr>
        </thead>
        <tbody>
          {entries?.map((e) => (
            <tr key={e.id}>
              <td>{e.type}</td>
              <td>{new Date(e.receivedAt).toLocaleString()}</td>
              <td className={expanded === e.id ? '' : 'truncate'} onClick={() => setExpanded(expanded === e.id ? null : e.id)}>
                {e.rawJsonData}
              </td>
            </tr>
          ))}
          {entries?.length === 0 && (
            <tr>
              <td colSpan={3}>No entries yet — trigger an ingest from the Data Sources tab.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
