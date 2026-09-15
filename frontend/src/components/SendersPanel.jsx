import { useState } from 'react'
import { createSender, deleteSender } from '../api/senders'
import { errorMessage } from '../hooks/useApi'

const SENDER_TYPES = ['EMAIL', 'SLACK', 'DISCORD']

const CONFIG_PLACEHOLDER = {
  EMAIL: 'email address',
  SLACK: 'Slack webhook URL',
  DISCORD: 'Discord webhook URL',
}

// Senders aren't given their own workflow.md section — they're "picked/created" while
// building an Alert (workflow.md §4) — so this panel manages them as reusable destinations.
export default function SendersPanel({ activeUserId, senders, loading, error, onChanged }) {
  const [type, setType] = useState('EMAIL')
  const [config, setConfig] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState(null)

  async function handleCreate(e) {
    e.preventDefault()
    setSubmitting(true)
    setFormError(null)
    try {
      await createSender({ type, userId: activeUserId, config })
      setConfig('')
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(id) {
    setFormError(null)
    try {
      await deleteSender(id)
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  if (!activeUserId) return <p>Select a user above to manage their senders.</p>

  return (
    <div className="panel">
      <h2>Senders</h2>
      <form className="inline-form" onSubmit={handleCreate}>
        <select value={type} onChange={(e) => setType(e.target.value)}>
          {SENDER_TYPES.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
        <input
          placeholder={CONFIG_PLACEHOLDER[type]}
          value={config}
          onChange={(e) => setConfig(e.target.value)}
          required
        />
        <button type="submit" disabled={submitting}>
          Add sender
        </button>
      </form>
      {formError && <p className="error">{formError}</p>}

      {loading && <p>Loading senders...</p>}
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Type</th>
            <th>Config</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {senders?.map((s) => (
            <tr key={s.id}>
              <td>{s.type}</td>
              <td>{s.config}</td>
              <td>
                <button type="button" onClick={() => handleDelete(s.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {senders?.length === 0 && (
            <tr>
              <td colSpan={3}>No senders yet — add one before creating an alert.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
