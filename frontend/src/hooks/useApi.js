import { useCallback, useEffect, useState } from 'react'

export function errorMessage(err) {
  return err.response?.data?.message ?? err.message ?? 'Unknown error'
}

// Loads `fetcher()` on mount and whenever `deps` change; `reload()` re-runs it on demand.
export function useApi(fetcher, deps = []) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const reload = useCallback(() => {
    setLoading(true)
    setError(null)
    fetcher()
      .then(setData)
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => {
    reload()
  }, [reload])

  return { data, loading, error, reload }
}
