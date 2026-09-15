import { Fragment, useState } from 'react'
import { deactivateUser } from '../api/users'
import { errorMessage } from '../hooks/useApi'
import { describeCriteria } from '../utils/alertCriteria'

// Admin "get all users" view, joined to their Alerts (mvp final.md §5, workflow.md §1) --
// each user's alerts arrive embedded in the same response, expanded per row on demand.
export default function UsersPanel({ users, loading, error, onChanged }) {
  const [actionError, setActionError] = useState(null)
  const [expandedUserId, setExpandedUserId] = useState(null)

  async function handleDeactivate(id) {
    setActionError(null)
    try {
      await deactivateUser(id)
      await onChanged()
    } catch (err) {
      setActionError(errorMessage(err))
    }
  }

  function toggleAlerts(userId) {
    setExpandedUserId((current) => (current === userId ? null : userId))
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
                    {expandedUserId === u.id ? 'Hide alerts' : `View alerts (${u.alerts.length})`}
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
                    {u.alerts.length === 0 ? (
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
                          {u.alerts.map((a) => (
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
