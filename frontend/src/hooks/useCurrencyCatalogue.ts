import { useEffect, useState } from 'react'
import { getCurrencies } from '../api/client'
import { ApiError } from '../api/types'
import type { Currency } from '../api/types'

const myr: Currency = { code: 'MYR', name: 'Malaysian Ringgit', symbol: 'RM' }

export function useCurrencyCatalogue() {
  const [currencies, setCurrencies] = useState<Currency[]>([myr])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    void getCurrencies(controller.signal).then((response) => {
      if (controller.signal.aborted) return
      setCurrencies(response.some((currency) => currency.code === myr.code) ? response : [myr, ...response])
      setError(null)
    }).catch((requestError: unknown) => {
      if (controller.signal.aborted || (requestError instanceof DOMException && requestError.name === 'AbortError')) return
      setError(requestError instanceof ApiError ? requestError.message : 'Currency options are unavailable. Prices are shown in MYR.')
    }).finally(() => {
      if (!controller.signal.aborted) setLoading(false)
    })
    return () => controller.abort()
  }, [reloadKey])

  const retry = () => {
    setLoading(true)
    setReloadKey((value) => value + 1)
  }

  return { currencies, loading, error, retry }
}
