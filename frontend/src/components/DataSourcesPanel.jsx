import { useState } from 'react'
import { createDataSource, deactivateDataSource, deleteDataSource, ingestDataSource } from '../api/dataSources'
import { errorMessage } from '../hooks/useApi'

const DATA_TYPES = ['NEWS', 'MARKET', 'DISASTER', 'WEATHER']
const RESOURCE_TYPES = ['JSON', 'XML']

// Admin "add data source via mapping UI" (mvp final.md §5) — fieldMapping is edited as raw
// JSON here rather than a dedicated field-by-field mapper, which is enough for a basic UI.
export default function DataSourcesPanel({ dataSources, loading, error, onChanged }) {
  const [form, setForm] = useState({ type: 'NEWS', link: '', apiKey: '', fieldMapping: '', resourceType: 'JSON' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState(null)
  const [ingestResult, setIngestResult] = useState(null)

  function setField(name, value) {
    setForm((f) => ({ ...f, [name]: value }))
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSubmitting(true)
    setFormError(null)
    try {
      await createDataSource(form)
      setForm({ type: 'NEWS', link: '', apiKey: '', fieldMapping: '', resourceType: 'JSON' })
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
      await deactivateDataSource(id)
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  async function handleDelete(id) {
    setFormError(null)
    try {
      await deleteDataSource(id)
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  async function handleIngest(id) {
    setFormError(null)
    setIngestResult(null)
    try {
      const entries = await ingestDataSource(id)
      setIngestResult(`Ingested ${entries.length} entr${entries.length === 1 ? 'y' : 'ies'} from source ${id}.`)
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  return (
    <div className="panel">
      <h2>Data Sources</h2>

      <form className="stacked-form" onSubmit={handleCreate}>
        <label>
          Type:{' '}
          <select value={form.type} onChange={(e) => setField('type', e.target.value)}>
            {DATA_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </label>
        <label>
          Link: <input value={form.link} onChange={(e) => setField('link', e.target.value)} required />
        </label>
        <label>
          Resource type:{' '}
          <select value={form.resourceType} onChange={(e) => setField('resourceType', e.target.value)}>
            {RESOURCE_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </label>
        <label>
          API key (optional): <input value={form.apiKey} onChange={(e) => setField('apiKey', e.target.value)} />
        </label>
        <label>
          Field mapping (raw field → interface field, JSON):
          <textarea
            rows={3}
            placeholder='{"title":"headline","description":"text"}'
            value={form.fieldMapping}
            onChange={(e) => setField('fieldMapping', e.target.value)}
          />
        </label>
        <button type="submit" disabled={submitting}>
          Create data source
        </button>
      </form>
      {formError && <p className="error">{formError}</p>}
      {ingestResult && <p className="hint">{ingestResult}</p>}

      {loading && <p>Loading data sources...</p>}
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Type</th>
            <th>Link</th>
            <th>Resource</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {dataSources?.map((ds) => (
            <tr key={ds.id}>
              <td>{ds.type}</td>
              <td className="truncate" title={ds.link}>
                {ds.link}
              </td>
              <td>{ds.resourceType}</td>
              <td>
                <span className={`badge ${ds.active ? 'badge-active' : 'badge-inactive'}`}>
                  {ds.active ? 'active' : 'inactive'}
                </span>
              </td>
              <td>
                <button type="button" onClick={() => handleIngest(ds.id)}>
                  Ingest now
                </button>
                {ds.active && (
                  <button type="button" onClick={() => handleDeactivate(ds.id)}>
                    Deactivate
                  </button>
                )}
                <button type="button" onClick={() => handleDelete(ds.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {dataSources?.length === 0 && (
            <tr>
              <td colSpan={5}>No data sources yet.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
