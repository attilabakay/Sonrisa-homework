import { Fragment, useState } from 'react'
import { deactivateUser } from '../api/users'
import { getAllAlertsByUser } from '../api/alerts'
import { errorMessage } from '../hooks/useApi'
import { describeCriteria } from '../utils/alertCriteria'

// Admin "get all users" view + enable/disable + "visibility into which alerts belong to
// which user" (mvp final.md §5, workflow.md §1) via the expandable row below.
export default function UsersPanel({ users, loading, error, onChanged }) {
  const [actionError, setActionError] = useState(null)
  const [expandedUserId, setExpandedUserId] = useState(null)
  const [alertsByUser, setAlertsByUser] = useState({})
  const [alertsLoading, setAlertsLoading] = useState(false)

  async function handleDeactivate(id) {
    setActionError(null)
    try {
      await deactivateUser(id)
      await onChanged()
    } catch (err) {
      setActionError(errorMessage(err))
    }
  }

  async function toggleAlerts(userId) {
    if (expandedUserId === userId) {
      setExpandedUserId(null)
      return
    }
    setExpandedUserId(userId)
    if (!alertsByUser[userId]) {
      setAlertsLoading(true)
      try {
        const alerts = await getAllAlertsByUser(userId)
        setAlertsByUser((prev) => ({ ...prev, [userId]: alerts }))
      } catch (err) {
        setActionError(errorMessage(err))
      } finally {
        setAlertsLoading(false)
      }
    }
  }

  if (loading) return <p>Loading users...</p>
  if (error) return <p className="error">{error}</p>

  return (
    <div className="panel">
      <h2>Users</h2>
      {actionError && <p className="error">{actionError}</p>}
      <table>
        <thead>
          <tr>
            <th>Email</th>
            <th>Admin</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {users?.map((u) => (
            <Fragment key={u.id}>
              <tr>
                <td>{u.email}</td>
                <td>{u.admin ? 'yes' : 'no'}</td>
                <td>
                  <span className={`badge ${u.active ? 'badge-active' : 'badge-inactive'}`}>
                    {u.active ? 'active' : 'disabled'}
                  </span>
                </td>
                <td>
                  <button type="button" onClick={() => toggleAlerts(u.id)}>
                    {expandedUserId === u.id ? 'Hide alerts' : 'View alerts'}
                  </button>
                  {u.active && (
                    <button type="button" onClick={() => handleDeactivate(u.id)}>
                      Disable
                    </button>
                  )}
                </td>
              </tr>
              {expandedUserId === u.id && (
                <tr>
                  <td colSpan={4}>
                    {alertsLoading && !alertsByUser[u.id] ? (
                      <p className="hint">Loading alerts...</p>
                    ) : (alertsByUser[u.id] ?? []).length === 0 ? (
                      <p className="hint">No alerts for this user.</p>
                    ) : (
                      <table>
                        <thead>
                          <tr>
                            <th>Type</th>
                            <th>Criteria</th>
                            <th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {alertsByUser[u.id].map((a) => (
                            <tr key={a.id}>
                              <td>{a.type}</td>
                              <td>{describeCriteria(a.type, a.criteria)}</td>
                              <td>
                                <span className={`badge ${a.active ? 'badge-active' : 'badge-inactive'}`}>
                                  {a.active ? 'active' : 'inactive'}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    )}
                  </td>
                </tr>
              )}
            </Fragment>
          ))}
          {users?.length === 0 && (
            <tr>
              <td colSpan={4}>No users yet.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
