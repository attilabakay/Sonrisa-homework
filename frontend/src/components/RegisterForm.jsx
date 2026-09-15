import { useState } from 'react'

export default function RegisterForm({ error, onSubmit }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [mismatch, setMismatch] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (password !== confirmPassword) {
      setMismatch(true)
      return
    }
    setMismatch(false)
    setSubmitting(true)
    await onSubmit(email, password)
    setSubmitting(false)
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <h2>Create an account</h2>
      <label>
        Email
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoFocus required />
      </label>
      <label>
        Password
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={8}
          required
        />
      </label>
      <label>
        Confirm password
        <input
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          minLength={8}
          required
        />
      </label>
      {mismatch && <p className="error">Passwords don't match.</p>}
      {error && <p className="error">{error}</p>}
      <button type="submit" disabled={submitting}>
        Create account
      </button>
      <p className="hint">At least 8 characters.</p>
    </form>
  )
}
