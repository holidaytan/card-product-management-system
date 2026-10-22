import { useEffect, useState } from 'react'
import { getCardProductSummary } from '../api/client'
import { ApiError } from '../api/types'
import type { CardProductSummary } from '../api/types'

export function useProductSummary() {
  const [data, setData] = useState<CardProductSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    void Promise.resolve().then(() => {
      setLoading(true)
      setError(null)
      return getCardProductSummary(controller.signal)
    }).then(setData).catch((requestError: unknown) => {
      if (!(requestError instanceof DOMException && requestError.name === 'AbortError')) {
        setError(requestError instanceof ApiError ? requestError.message : 'Unable to load dashboard summary.')
      }
    }).finally(() => setLoading(false))
    return () => controller.abort()
  }, [reloadKey])

  return { data, loading, error, reload: () => setReloadKey((value) => value + 1) }
}
