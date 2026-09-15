import { useState } from 'react'

// Login/register control for HTTP Basic auth (useAuth owns the actual credential handling —
// this just collects email/password and shows who's currently logged in).
export default function UserBar({ currentUser, error, onLogin, onRegister, onLogout }) {
  const [mode, setMode] = useState('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    const ok = mode === 'login' ? await onLogin(email, password) : await onRegister(email, password)
    setSubmitting(false)
    if (ok) {
      setEmail('')
      setPassword('')
    }
  }

  if (currentUser) {
    return (
      <div className="user-bar">
        <div className="user-bar-row">
          <span>Logged in as {currentUser.email}</span>
          <span className={`badge ${currentUser.admin ? 'badge-admin' : ''}`}>
            {currentUser.admin ? 'admin' : 'user'}
          </span>
          <button type="button" onClick={onLogout}>
            Log out
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="user-bar">
      <div className="user-bar-row">
        <button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>
          Log in
        </button>
        <button type="button" className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>
          Register
        </button>
      </div>
      <form className="inline-form" onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder={mode === 'register' ? 'password (min 8 chars)' : 'password'}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={mode === 'register' ? 8 : undefined}
          required
        />
        <button type="submit" disabled={submitting}>
          {mode === 'login' ? 'Log in' : 'Register'}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
    </div>
  )
}
