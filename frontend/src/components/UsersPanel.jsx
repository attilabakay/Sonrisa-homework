import { useState } from 'react'
import { deactivateUser } from '../api/users'
import { errorMessage } from '../hooks/useApi'

// Admin "get all users" view + enable/disable (mvp final.md §5, workflow.md §1).
export default function UsersPanel({ users, loading, error, onChanged }) {
  const [actionError, setActionError] = useState(null)

  async function handleDeactivate(id) {
    setActionError(null)
    try {
      await deactivateUser(id)
      await onChanged()
    } catch (err) {
      setActionError(errorMessage(err))
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
            <tr key={u.id}>
              <td>{u.email}</td>
              <td>{u.admin ? 'yes' : 'no'}</td>
              <td>
                <span className={`badge ${u.active ? 'badge-active' : 'badge-inactive'}`}>
                  {u.active ? 'active' : 'disabled'}
                </span>
              </td>
              <td>
                {u.active && (
                  <button type="button" onClick={() => handleDeactivate(u.id)}>
                    Disable
                  </button>
                )}
              </td>
            </tr>
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
