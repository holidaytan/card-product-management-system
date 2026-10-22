import { useEffect, useState } from 'react'
import { getCardProducts } from '../api/client'
import type { ProductDirectoryFilters } from '../api/client'
import { ApiError } from '../api/types'
import type { CardProductPage } from '../api/types'

export function useCardProducts(page: number, filters: ProductDirectoryFilters) {
  const [data, setData] = useState<CardProductPage | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    void Promise.resolve().then(() => {
      setLoading(true)
      setError(null)
      return getCardProducts(page, filters, controller.signal)
    }).then(setData).catch((requestError: unknown) => {
      if (!(requestError instanceof DOMException && requestError.name === 'AbortError')) {
        setError(requestError instanceof ApiError ? requestError.message : 'Unable to load card products.')
      }
    }).finally(() => setLoading(false))
    return () => controller.abort()
  }, [page, filters, reloadKey])

  return { data, loading, error, reload: () => setReloadKey((value) => value + 1) }
}
