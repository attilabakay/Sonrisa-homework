import { Fragment, useState } from 'react'
import { createDataSource, deactivateDataSource, deleteDataSource, ingestDataSource, updateDataSource } from '../api/dataSources'
import { errorMessage } from '../hooks/useApi'
import KebabMenu from './KebabMenu'

const DATA_TYPES = ['NEWS', 'MARKET', 'DISASTER', 'WEATHER']
const RESOURCE_TYPES = ['JSON', 'XML']
const EMPTY_FORM = { type: 'NEWS', link: '', apiKey: '', fieldMapping: '', resourceType: 'JSON' }

function parseMapping(fieldMappingJson) {
  try {
    return Object.entries(JSON.parse(fieldMappingJson || '{}'))
  } catch {
    return null
  }
}

function summarizeMapping(fieldMappingJson) {
  const entries = parseMapping(fieldMappingJson)
  if (entries === null) return fieldMappingJson || '(no mapping set)'
  if (entries.length === 0) return '(no mapping set)'
  return entries.map(([raw, iface]) => `${raw} → ${iface}`).join(', ')
}

// Admin "add data source via mapping UI" (mvp final.md §5) — fieldMapping is edited as raw
// JSON here rather than a dedicated field-by-field mapper, which is enough for a basic UI.
export default function DataSourcesPanel({ dataSources, loading, error, onChanged }) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState(null)
  const [ingestResult, setIngestResult] = useState(null)

  // A row is either collapsed, showing its details, or showing an edit form for those details.
  const [openRowId, setOpenRowId] = useState(null)
  const [rowMode, setRowMode] = useState('view') // 'view' | 'edit'
  const [editForm, setEditForm] = useState(null)
  const [rowError, setRowError] = useState(null)
  const [rowSubmitting, setRowSubmitting] = useState(false)

  function setField(name, value) {
    setForm((f) => ({ ...f, [name]: value }))
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSubmitting(true)
    setFormError(null)
    try {
      await createDataSource(form)
      setForm(EMPTY_FORM)
      await onChanged()
    } catch (err) {
      setFormError(errorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  function toggleDetails(ds) {
    if (openRowId === ds.id) {
      setOpenRowId(null)
      return
    }
    setOpenRowId(ds.id)
    setRowMode('view')
    setRowError(null)
  }

  function startEdit(ds) {
    setOpenRowId(ds.id)
    setRowMode('edit')
    setRowError(null)
    setEditForm({
      type: ds.type,
      link: ds.link,
      apiKey: ds.apiKey ?? '',
      fieldMapping: ds.fieldMapping ?? '',
      resourceType: ds.resourceType,
    })
  }

  function setEditField(name, value) {
    setEditForm((f) => ({ ...f, [name]: value }))
  }

  async function handleSaveEdit(id) {
    setRowSubmitting(true)
    setRowError(null)
    try {
      await updateDataSource(id, editForm)
      setRowMode('view')
      await onChanged()
    } catch (err) {
      setRowError(errorMessage(err))
    } finally {
      setRowSubmitting(false)
    }
  }

  async function handleDeactivate(id) {
    setRowError(null)
    try {
      await deactivateDataSource(id)
      await onChanged()
    } catch (err) {
      setRowError(errorMessage(err))
    }
  }

  async function handleDelete(id) {
    setRowError(null)
    try {
      await deleteDataSource(id)
      setOpenRowId(null)
      await onChanged()
    } catch (err) {
      setRowError(errorMessage(err))
    }
  }

  async function handleIngest(ds) {
    setRowError(null)
    setIngestResult(null)
    try {
      const entries = await ingestDataSource(ds.id)
      setIngestResult(`Ingested ${entries.length} entr${entries.length === 1 ? 'y' : 'ies'} from ${ds.type.toLowerCase()} source ${ds.link}.`)
      await onChanged()
    } catch (err) {
      setRowError(errorMessage(err))
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
            <th>Field mapping</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {dataSources?.map((ds) => (
            <Fragment key={ds.id}>
              <tr>
                <td>{ds.type}</td>
                <td className="truncate" title={ds.link}>
                  {ds.link}
                </td>
                <td>{ds.resourceType}</td>
                <td>
                  <button type="button" className="mapping-link" onClick={() => toggleDetails(ds)}>
                    {summarizeMapping(ds.fieldMapping)}
                  </button>
                </td>
                <td>
                  <span className={`badge ${ds.active ? 'badge-active' : 'badge-inactive'}`}>
                    {ds.active ? 'active' : 'inactive'}
                  </span>
                </td>
                <td>
                  <KebabMenu
                    items={[
                      { label: 'View details', onClick: () => toggleDetails(ds) },
                      { label: 'Edit', onClick: () => startEdit(ds) },
                      { label: 'Ingest now', onClick: () => handleIngest(ds) },
                      ...(ds.active ? [{ label: 'Deactivate', onClick: () => handleDeactivate(ds.id) }] : []),
                      { label: 'Delete', onClick: () => handleDelete(ds.id), danger: true },
                    ]}
                  />
                </td>
              </tr>
              {openRowId === ds.id && (
                <tr>
                  <td colSpan={6}>
                    <div className="detail-panel">
                      {rowError && <p className="error">{rowError}</p>}

                      {rowMode === 'view' ? (
                        <>
                          <dl className="detail-fields">
                            <dt>Link</dt>
                            <dd>{ds.link}</dd>
                            <dt>Resource type</dt>
                            <dd>{ds.resourceType}</dd>
                            <dt>API key</dt>
                            <dd>{ds.apiKey ? '••••' + ds.apiKey.slice(-4) : '(not set)'}</dd>
                          </dl>
                          {parseMapping(ds.fieldMapping)?.length ? (
                            <table className="mapping-table">
                              <thead>
                                <tr>
                                  <th>Provider field</th>
                                  <th>Interface field</th>
                                </tr>
                              </thead>
                              <tbody>
                                {parseMapping(ds.fieldMapping).map(([raw, iface]) => (
                                  <tr key={raw}>
                                    <td>{raw}</td>
                                    <td>{iface}</td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          ) : (
                            <p className="hint">No fields mapped yet.</p>
                          )}
                        </>
                      ) : (
                        <div className="stacked-form">
                          <label>
                            Type:{' '}
                            <select value={editForm.type} onChange={(e) => setEditField('type', e.target.value)}>
                              {DATA_TYPES.map((t) => (
                                <option key={t} value={t}>
                                  {t}
                                </option>
                              ))}
                            </select>
                          </label>
                          <label>
                            Link: <input value={editForm.link} onChange={(e) => setEditField('link', e.target.value)} required />
                          </label>
                          <label>
                            Resource type:{' '}
                            <select value={editForm.resourceType} onChange={(e) => setEditField('resourceType', e.target.value)}>
                              {RESOURCE_TYPES.map((t) => (
                                <option key={t} value={t}>
                                  {t}
                                </option>
                              ))}
                            </select>
                          </label>
                          <label>
                            API key (optional):{' '}
                            <input value={editForm.apiKey} onChange={(e) => setEditField('apiKey', e.target.value)} />
                          </label>
                          <label>
                            Field mapping (raw field → interface field, JSON):
                            <textarea
                              rows={3}
                              value={editForm.fieldMapping}
                              onChange={(e) => setEditField('fieldMapping', e.target.value)}
                            />
                          </label>
                          <div>
                            <button type="button" disabled={rowSubmitting} onClick={() => handleSaveEdit(ds.id)}>
                              Save changes
                            </button>
                            <button type="button" onClick={() => setRowMode('view')}>
                              Cancel
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  </td>
                </tr>
              )}
            </Fragment>
          ))}
          {dataSources?.length === 0 && (
            <tr>
              <td colSpan={6}>No data sources yet.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
