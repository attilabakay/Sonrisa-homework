// Admin "get all" view of NotificationAttempts — the debugging surface for "did this fire,
// did it succeed" (workflow.md §5).
export default function NotificationsPanel({ attempts, loading, error }) {
  return (
    <div className="panel">
      <h2>Notification Attempts</h2>

      {loading && <p>Loading notification attempts...</p>}
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Sent at</th>
            <th>Alert</th>
            <th>Data entry</th>
            <th>Status</th>
            <th>Error</th>
          </tr>
        </thead>
        <tbody>
          {attempts?.map((a) => (
            <tr key={a.id}>
              <td>{new Date(a.sentAt).toLocaleString()}</td>
              <td title={a.alertId}>{a.alertId.slice(0, 8)}</td>
              <td title={a.dataEntryId}>{a.dataEntryId.slice(0, 8)}</td>
              <td>
                <span className={`badge ${a.status === 'SENT' ? 'badge-active' : 'badge-inactive'}`}>{a.status}</span>
              </td>
              <td className="truncate" title={a.error ?? ''}>
                {a.error ?? ''}
              </td>
            </tr>
          ))}
          {attempts?.length === 0 && (
            <tr>
              <td colSpan={5}>No notification attempts yet.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
