import { ApiError } from './types'
import type { CardProduct, CardProductPage, CardProductSummary, CreateCardProductRequest, Currency, ExchangeRate, UpdateCardProductRequest } from './types'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export interface ProductDirectoryFilters {
  name: string
  cardType: 'CREDIT' | 'DEBIT' | null
}

export function getCardProducts(page: number, filters: ProductDirectoryFilters, signal?: AbortSignal): Promise<CardProductPage> {
  const query = new URLSearchParams({ page: String(page) })
  if (filters.name) query.set('name', filters.name)
  if (filters.cardType) query.set('type', filters.cardType)
  return request<CardProductPage>(`/api/v1/card-products?${query}`, { signal })
}

export function getCurrencies(signal?: AbortSignal): Promise<Currency[]> {
  return request<Currency[]>('/api/v1/exchange-rates/currencies', { signal })
}

export function getCardProductSummary(signal?: AbortSignal): Promise<CardProductSummary> {
  return request<CardProductSummary>('/api/v1/card-products/summary', { signal })
}

export function getExchangeRate(to: string, signal?: AbortSignal): Promise<ExchangeRate> {
  return request<ExchangeRate>(`/api/v1/exchange-rates?to=${encodeURIComponent(to)}`, { signal })
}

export function createCardProduct(requestBody: CreateCardProductRequest, idempotencyKey: string): Promise<CardProduct> {
  return request<CardProduct>('/api/v1/card-products', {
    method: 'POST', body: requestBody, headers: { 'Idempotency-Key': idempotencyKey },
  })
}

export function updateCardProduct(id: number, requestBody: UpdateCardProductRequest): Promise<CardProduct> {
  return request<CardProduct>(`/api/v1/card-products/${id}`, { method: 'PUT', body: requestBody })
}

export function deleteCardProduct(id: number): Promise<void> {
  return request<void>(`/api/v1/card-products/${id}`, { method: 'DELETE' })
}

interface RequestOptions {
  method?: 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  headers?: Record<string, string>
  signal?: AbortSignal
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json', ...options.headers }
  if (options.body !== undefined) headers['Content-Type'] = 'application/json'
  const response = await fetch(`${apiBaseUrl}${path}`, {
    method: options.method, headers, body: options.body === undefined ? undefined : JSON.stringify(options.body), signal: options.signal,
  })
  if (!response.ok) {
    const problem = (await response.json().catch(() => ({}))) as { detail?: string; title?: string; errors?: unknown }
    const errors = Array.isArray(problem.errors) ? problem.errors.filter((error): error is string => typeof error === 'string') : []
    throw new ApiError(problem.detail ?? problem.title ?? 'The request could not be completed.', response.status, errors)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
