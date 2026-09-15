import { useCallback, useEffect, useState } from 'react'
import client from '../api/client'
import { getMe, registerUser } from '../api/users'
import { errorMessage } from './useApi'

const STORAGE_KEY = 'authCredentials'

// HTTP Basic auth (mvp final.md §1) — no login endpoint. "Logging in" means attaching an
// Authorization header to every request and confirming it with GET /users/me. Credentials
// are kept in sessionStorage (cleared when the tab closes) purely so a page refresh doesn't
// drop the session; this is a demo-grade tradeoff, not something to reuse for a real app.
export function useAuth() {
  const [currentUser, setCurrentUser] = useState(null)
  const [error, setError] = useState(null)
  const [initializing, setInitializing] = useState(true)

  const login = useCallback(async (email, password) => {
    setError(null)
    client.defaults.auth = { username: email, password }
    try {
      const user = await getMe()
      setCurrentUser(user)
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ email, password }))
      return true
    } catch (err) {
      delete client.defaults.auth
      setError(err.response?.status === 401 ? 'Invalid email or password' : errorMessage(err))
      return false
    }
  }, [])

  const register = useCallback(
    async (email, password) => {
      setError(null)
      try {
        await registerUser({ email, password })
        return login(email, password)
      } catch (err) {
        setError(errorMessage(err))
        return false
      }
    },
    [login],
  )

  const logout = useCallback(() => {
    delete client.defaults.auth
    sessionStorage.removeItem(STORAGE_KEY)
    setCurrentUser(null)
  }, [])

  useEffect(() => {
    const stored = sessionStorage.getItem(STORAGE_KEY)
    if (!stored) {
      setInitializing(false)
      return
    }
    const { email, password } = JSON.parse(stored)
    login(email, password).finally(() => setInitializing(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return { currentUser, error, initializing, login, register, logout }
}
