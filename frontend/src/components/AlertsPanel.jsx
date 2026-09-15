import { useState } from 'react'
import { createAlert, deactivateAlert, deleteAlert } from '../api/alerts'
import { errorMessage } from '../hooks/useApi'
import { describeCriteria } from '../utils/alertCriteria'

const ALERT_TYPES = ['NEWS', 'MARKET', 'DISASTER', 'WEATHER']

// Three alert types, each with its own criteria shape (mvp final.md §2) — criteria is stored
// as a raw JSON string on the backend, built here from the type-specific fields below.
function buildCriteria(type, fields) {
  if (type === 'NEWS') return { keyword: fields.keyword }
  if (type === 'MARKET') return { ticker: fields.ticker, comparator: fields.comparator, threshold: Number(fields.threshold) }
  return { region: fields.region }
}

export default function AlertsPanel({ activeUserId, isAdmin, senders, alerts, loading, error, showAll, onToggleShowAll, onChanged }) {
  const [type, setType] = useState('NEWS')
  const [senderId, setSenderId] = useState('')
  const [fields, setFields] = useState({ keyword: '', ticker: '', comparator: 'ABOVE', threshold: '', region: '' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState(null)

  function setField(name, value) {
    setFields((f) => ({ ...f, [name]: value }))
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSubmitting(true)
    setFormError(null)
    try {
      await createAlert({
        type,
        criteria: JSON.stringify(buildCriteria(type, fields)),
        userId: activeUserId,
        senderId,
      })
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDeactivate(id) {
    setFormError(null)
    try {
      await deactivateAlert(id)
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  async function handleDelete(id) {
    setFormError(null)
    try {
      await deleteAlert(id)
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  if (!activeUserId) return <p>Select a user above to manage their alerts.</p>

  return (
    <div className="panel">
      <h2>Alerts</h2>

      <form className="stacked-form" onSubmit={handleCreate}>
        <label>
          Type:{' '}
          <select value={type} onChange={(e) => setType(e.target.value)}>
            {ALERT_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </label>

        {type === 'NEWS' && (
          <label>
            Keyword:{' '}
            <input value={fields.keyword} onChange={(e) => setField('keyword', e.target.value)} required />
          </label>
        )}

        {type === 'MARKET' && (
          <>
            <label>
              Ticker: <input value={fields.ticker} onChange={(e) => setField('ticker', e.target.value)} required />
            </label>
            <label>
              Comparator:{' '}
              <select value={fields.comparator} onChange={(e) => setField('comparator', e.target.value)}>
                <option value="ABOVE">ABOVE</option>
                <option value="BELOW">BELOW</option>
              </select>
            </label>
            <label>
              Threshold:{' '}
              <input
                type="number"
                step="any"
                value={fields.threshold}
                onChange={(e) => setField('threshold', e.target.value)}
                required
              />
            </label>
          </>
        )}

        {(type === 'DISASTER' || type === 'WEATHER') && (
          <label>
            Region: <input value={fields.region} onChange={(e) => setField('region', e.target.value)} required />
          </label>
        )}

        <label>
          Sender:{' '}
          <select value={senderId} onChange={(e) => setSenderId(e.target.value)} required>
            <option value="">-- select sender --</option>
            {senders?.map((s) => (
              <option key={s.id} value={s.id}>
                {s.type}: {s.config}
              </option>
            ))}
          </select>
        </label>

        <button type="submit" disabled={submitting || !senders?.length}>
          Create alert
        </button>
        {!senders?.length && <span className="hint">Add a sender first.</span>}
      </form>
      {formError && <p className="error">{formError}</p>}

      {isAdmin && (
        <label className="hint">
          <input type="checkbox" checked={showAll} onChange={(e) => onToggleShowAll(e.target.checked)} /> show
          inactive alerts too
        </label>
      )}

      {loading && <p>Loading alerts...</p>}
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Type</th>
            <th>Criteria</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {alerts?.map((a) => (
            <tr key={a.id}>
              <td>{a.type}</td>
              <td>{describeCriteria(a.type, a.criteria)}</td>
              <td>
                <span className={`badge ${a.active ? 'badge-active' : 'badge-inactive'}`}>
                  {a.active ? 'active' : 'inactive'}
                </span>
              </td>
              <td>
                {a.active && (
                  <button type="button" onClick={() => handleDeactivate(a.id)}>
                    Deactivate
                  </button>
                )}
                <button type="button" onClick={() => handleDelete(a.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {alerts?.length === 0 && (
            <tr>
              <td colSpan={4}>No alerts yet.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
