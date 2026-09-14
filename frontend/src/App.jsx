import { useEffect, useState } from 'react'
import './App.css'
import { getUsers } from './api/users'
import { getSendersByUser } from './api/senders'
import { getActiveAlertsByUser, getAllAlertsByUser } from './api/alerts'
import { getDataSources } from './api/dataSources'
import { getDataEntries, getDataEntriesBySource } from './api/dataEntries'
import { getNotificationAttempts } from './api/notifications'
import { useApi } from './hooks/useApi'
import UserBar from './components/UserBar'
import UsersPanel from './components/UsersPanel'
import SendersPanel from './components/SendersPanel'
import AlertsPanel from './components/AlertsPanel'
import DataSourcesPanel from './components/DataSourcesPanel'
import DataEntriesPanel from './components/DataEntriesPanel'
import NotificationsPanel from './components/NotificationsPanel'

const TABS = ['Alerts', 'Senders', 'Data Sources', 'Data Entries', 'Notifications', 'Users']

export default function App() {
  const [tab, setTab] = useState('Alerts')
  const [activeUserId, setActiveUserId] = useState(() => localStorage.getItem('activeUserId'))
  const [showAllAlerts, setShowAllAlerts] = useState(false)
  const [entrySourceFilter, setEntrySourceFilter] = useState('')

  useEffect(() => {
    if (activeUserId) localStorage.setItem('activeUserId', activeUserId)
    else localStorage.removeItem('activeUserId')
  }, [activeUserId])

  const usersApi = useApi(getUsers, [])
  const sendersApi = useApi(
    () => (activeUserId ? getSendersByUser(activeUserId) : Promise.resolve([])),
    [activeUserId],
  )
  const alertsApi = useApi(
    () =>
      activeUserId
        ? showAllAlerts
          ? getAllAlertsByUser(activeUserId)
          : getActiveAlertsByUser(activeUserId)
        : Promise.resolve([]),
    [activeUserId, showAllAlerts],
  )
  const dataSourcesApi = useApi(getDataSources, [])
  const dataEntriesApi = useApi(
    () => (entrySourceFilter ? getDataEntriesBySource(entrySourceFilter) : getDataEntries()),
    [entrySourceFilter],
  )
  const notificationsApi = useApi(getNotificationAttempts, [])

  // Friendlier hint if the backend isn't reachable at all (e.g. not started yet).
  const connectionError =
    usersApi.error && /Network Error/i.test(usersApi.error)
      ? 'Cannot reach the backend at ' + (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api') + '. Is it running?'
      : null

  async function refreshUsersAndDependents() {
    await usersApi.reload()
  }

  return (
    <div className="app">
      <header>
        <h1>World Event Alerts</h1>
        <UserBar
          users={usersApi.data ?? []}
          activeUserId={activeUserId}
          onSelect={setActiveUserId}
          onUsersChanged={refreshUsersAndDependents}
        />
      </header>

      {connectionError && <p className="error connection-error">{connectionError}</p>}

      <nav className="tabs">
        {TABS.map((t) => (
          <button key={t} type="button" className={t === tab ? 'active' : ''} onClick={() => setTab(t)}>
            {t}
          </button>
        ))}
      </nav>

      <main>
        {tab === 'Alerts' && (
          <AlertsPanel
            activeUserId={activeUserId}
            senders={sendersApi.data}
            alerts={alertsApi.data}
            loading={alertsApi.loading}
            error={alertsApi.error}
            showAll={showAllAlerts}
            onToggleShowAll={setShowAllAlerts}
            onChanged={alertsApi.reload}
          />
        )}

        {tab === 'Senders' && (
          <SendersPanel
            activeUserId={activeUserId}
            senders={sendersApi.data}
            loading={sendersApi.loading}
            error={sendersApi.error}
            onChanged={sendersApi.reload}
          />
        )}

        {tab === 'Data Sources' && (
          <DataSourcesPanel
            dataSources={dataSourcesApi.data}
            loading={dataSourcesApi.loading}
            error={dataSourcesApi.error}
            onChanged={async () => {
              await dataSourcesApi.reload()
              await dataEntriesApi.reload()
              await notificationsApi.reload()
            }}
          />
        )}

        {tab === 'Data Entries' && (
          <DataEntriesPanel
            dataSources={dataSourcesApi.data}
            entries={dataEntriesApi.data}
            loading={dataEntriesApi.loading}
            error={dataEntriesApi.error}
            sourceFilter={entrySourceFilter}
            onFilterChange={setEntrySourceFilter}
          />
        )}

        {tab === 'Notifications' && (
          <NotificationsPanel attempts={notificationsApi.data} loading={notificationsApi.loading} error={notificationsApi.error} />
        )}

        {tab === 'Users' && (
          <UsersPanel users={usersApi.data} loading={usersApi.loading} error={usersApi.error} onChanged={usersApi.reload} />
        )}
      </main>
    </div>
  )
}
