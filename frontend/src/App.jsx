import { useEffect, useState } from 'react'
import './App.css'
import { getUsers } from './api/users'
import { getSendersByUser } from './api/senders'
import { getActiveAlertsByUser, getAllAlertsByUser } from './api/alerts'
import { getDataSources } from './api/dataSources'
import { getDataEntries, getDataEntriesBySource } from './api/dataEntries'
import { getNotificationAttempts } from './api/notifications'
import { useApi } from './hooks/useApi'
import { useAuth } from './hooks/useAuth'
import UserBar from './components/UserBar'
import UsersPanel from './components/UsersPanel'
import SendersPanel from './components/SendersPanel'
import AlertsPanel from './components/AlertsPanel'
import DataSourcesPanel from './components/DataSourcesPanel'
import DataEntriesPanel from './components/DataEntriesPanel'
import NotificationsPanel from './components/NotificationsPanel'

// User-facing (workflow.md: Alert/Sender are the user's own) vs admin-facing (User,
// DataSource, DataEntry, NotificationAttempt are all admin/system-only per workflow.md).
const USER_TABS = ['Alerts', 'Senders']
const ADMIN_TABS = ['Data Sources', 'Data Entries', 'Notifications', 'Users']

export default function App() {
  const { currentUser, error: authError, initializing, login, register, logout } = useAuth()
  const [tab, setTab] = useState('Alerts')
  const [showAllAlerts, setShowAllAlerts] = useState(false)
  const [entrySourceFilter, setEntrySourceFilter] = useState('')

  const loggedIn = Boolean(currentUser)
  const isAdmin = currentUser?.admin === true
  const visibleTabs = isAdmin ? [...USER_TABS, ...ADMIN_TABS] : USER_TABS

  // If role changes (e.g. logging out of an admin account and into a regular one) while an
  // admin-only tab is selected, fall back to a tab everyone can see.
  useEffect(() => {
    if (!visibleTabs.includes(tab)) setTab('Alerts')
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAdmin, tab])

  const usersApi = useApi(() => (isAdmin ? getUsers() : Promise.resolve([])), [isAdmin])
  const sendersApi = useApi(
    () => (loggedIn ? getSendersByUser(currentUser.id) : Promise.resolve([])),
    [loggedIn, currentUser?.id],
  )
  const alertsApi = useApi(
    () =>
      !loggedIn
        ? Promise.resolve([])
        : showAllAlerts && isAdmin
          ? getAllAlertsByUser(currentUser.id)
          : getActiveAlertsByUser(currentUser.id),
    [loggedIn, currentUser?.id, showAllAlerts, isAdmin],
  )
  const dataSourcesApi = useApi(() => (isAdmin ? getDataSources() : Promise.resolve([])), [isAdmin])
  const dataEntriesApi = useApi(
    () => (!isAdmin ? Promise.resolve([]) : entrySourceFilter ? getDataEntriesBySource(entrySourceFilter) : getDataEntries()),
    [isAdmin, entrySourceFilter],
  )
  const notificationsApi = useApi(() => (isAdmin ? getNotificationAttempts() : Promise.resolve([])), [isAdmin])

  // Friendlier hint if the backend isn't reachable at all (e.g. not started yet).
  const connectionError =
    authError && /Network Error/i.test(authError)
      ? 'Cannot reach the backend at ' + (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api') + '. Is it running?'
      : null

  return (
    <div className="app">
      <header>
        <h1>World Event Alerts</h1>
        <UserBar currentUser={currentUser} error={authError} onLogin={login} onRegister={register} onLogout={logout} />
      </header>

      {connectionError && <p className="error connection-error">{connectionError}</p>}

      {initializing && <p>Checking session...</p>}

      {!initializing && !loggedIn && <p className="hint">Log in or register above to continue.</p>}

      {!initializing && loggedIn && (
        <>
          <nav className="tabs">
            {visibleTabs.map((t) => (
              <button key={t} type="button" className={t === tab ? 'active' : ''} onClick={() => setTab(t)}>
                {t}
              </button>
            ))}
          </nav>

          <main>
            {tab === 'Alerts' && (
              <AlertsPanel
                activeUserId={currentUser.id}
                isAdmin={isAdmin}
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
                activeUserId={currentUser.id}
                senders={sendersApi.data}
                loading={sendersApi.loading}
                error={sendersApi.error}
                onChanged={sendersApi.reload}
              />
            )}

            {tab === 'Data Sources' && isAdmin && (
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

            {tab === 'Data Entries' && isAdmin && (
              <DataEntriesPanel
                dataSources={dataSourcesApi.data}
                entries={dataEntriesApi.data}
                loading={dataEntriesApi.loading}
                error={dataEntriesApi.error}
                sourceFilter={entrySourceFilter}
                onFilterChange={setEntrySourceFilter}
              />
            )}

            {tab === 'Notifications' && isAdmin && (
              <NotificationsPanel
                attempts={notificationsApi.data}
                loading={notificationsApi.loading}
                error={notificationsApi.error}
              />
            )}

            {tab === 'Users' && isAdmin && (
              <UsersPanel users={usersApi.data} loading={usersApi.loading} error={usersApi.error} onChanged={usersApi.reload} />
            )}
          </main>
        </>
      )}
    </div>
  )
}
