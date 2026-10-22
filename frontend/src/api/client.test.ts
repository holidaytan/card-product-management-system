import { describe, expect, it, vi } from 'vitest'
import { createCardProduct, getCardProducts } from './client'

describe('API client', () => {
  it('serializes product directory filters in the request URL', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ content: [] }), { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await getCardProducts(2, { name: ' platinum ', cardType: 'CREDIT' })

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/card-products?page=2&name=+platinum+&type=CREDIT',
      expect.objectContaining({ method: undefined, headers: { Accept: 'application/json' } }),
    )
  })

  it('sends an idempotency key when creating a product', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ id: 1 }), { status: 201 }))
    vi.stubGlobal('fetch', fetchMock)

    await createCardProduct({ productName: 'CardOps Gold', cardType: 'CREDIT', quantity: 5, annualFee: 120, annualInterestRate: 18 }, 'request-123')

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/card-products',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({ 'Content-Type': 'application/json', 'Idempotency-Key': 'request-123' }),
        body: JSON.stringify({ productName: 'CardOps Gold', cardType: 'CREDIT', quantity: 5, annualFee: 120, annualInterestRate: 18 }),
      }),
    )
  })

  it('maps RFC 9457 API errors into ApiError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ detail: 'Exchange-rate provider is currently unavailable', errors: ['provider unavailable'] }), { status: 503 })))

    await expect(getCardProducts(0, { name: '', cardType: null })).rejects.toMatchObject(
      { status: 503, message: 'Exchange-rate provider is currently unavailable', errors: ['provider unavailable'] },
    )
  })
})
