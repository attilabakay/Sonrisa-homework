import { useCallback, useEffect, useState } from 'react'

export function errorMessage(err) {
  return err.response?.data?.message ?? err.message ?? 'Unknown error'
}

// Loads `fetcher()` on mount and whenever `deps` change; `reload()` re-runs it on demand.
// Pass `pollMs` to also re-run it in the background on that interval (e.g. so a tab reflects
// what the scheduled ingestion job did without the user reloading the page) — poll-triggered
// reloads are silent (no loading flicker, current data stays on screen until fresh data
// arrives); `reload()` called directly still shows the loading state as before.
export function useApi(fetcher, deps = [], { pollMs } = {}) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const reload = useCallback((opts = {}) => {
    const silent = opts.silent === true
    if (!silent) setLoading(true)
    setError(null)
    fetcher()
      .then(setData)
      .catch((e) => setError(errorMessage(e)))
      .finally(() => {
        if (!silent) setLoading(false)
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => {
    reload()
  }, [reload])

  useEffect(() => {
    if (!pollMs) return
    const id = setInterval(() => reload({ silent: true }), pollMs)
    return () => clearInterval(id)
  }, [pollMs, reload])

  return { data, loading, error, reload }
}
