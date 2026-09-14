import { useState } from 'react'
import { registerUser } from '../api/users'
import { errorMessage } from '../hooks/useApi'

// Stand-in for real login (mvp final.md §1 defers a login flow) — lets you register a user
// and pick which existing user you're "acting as" for the Alerts/Senders tabs.
export default function UserBar({ users, activeUserId, onSelect, onUsersChanged }) {
  const [showRegister, setShowRegister] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const activeUser = users?.find((u) => u.id === activeUserId) ?? null

  async function handleRegister(e) {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const user = await registerUser({ email, password })
      setEmail('')
      setPassword('')
      setShowRegister(false)
      await onUsersChanged()
      onSelect(user.id)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="user-bar">
      <div className="user-bar-row">
        <label>
          Acting as:{' '}
          <select value={activeUserId ?? ''} onChange={(e) => onSelect(e.target.value || null)}>
            <option value="">-- select user --</option>
            {users?.map((u) => (
              <option key={u.id} value={u.id} disabled={!u.active}>
                {u.email}
                {!u.active ? ' (disabled)' : ''}
              </option>
            ))}
          </select>
        </label>
        <button type="button" onClick={() => setShowRegister((v) => !v)}>
          {showRegister ? 'Cancel' : '+ New user'}
        </button>
        {activeUser && (
          <span className={`badge ${activeUser.admin ? 'badge-admin' : ''}`}>
            {activeUser.admin ? 'admin' : 'user'}
          </span>
        )}
      </div>

      {showRegister && (
        <form className="inline-form" onSubmit={handleRegister}>
          <input
            type="email"
            placeholder="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="password (min 8 chars)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            required
          />
          <button type="submit" disabled={submitting}>
            Register
          </button>
        </form>
      )}
      {error && <p className="error">{error}</p>}
    </div>
  )
}
