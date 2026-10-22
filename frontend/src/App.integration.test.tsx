import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { setupServer } from 'msw/node'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import App from './App'

const apiUrl = 'http://localhost:8080/api/v1'
let filteredRequestObserved = false
const server = setupServer(
  http.get(`${apiUrl}/card-products`, ({ request }) => {
    filteredRequestObserved ||= new URL(request.url).searchParams.get('name') === 'Plat'
    return HttpResponse.json({
    content: [
      { id: 1, productName: 'CardOps Platinum', cardType: 'CREDIT', quantity: 4, annualFee: 300, annualInterestRate: 18, currencyCode: 'MYR', createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z', version: 0 },
      { id: 2, productName: 'CardOps Debit', cardType: 'DEBIT', quantity: 0, annualFee: 20, annualInterestRate: null, currencyCode: 'MYR', createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z', version: 0 },
    ],
    page: 0, size: 10, totalElements: 2, totalPages: 1, first: true, last: true,
    })
  }),
  http.get(`${apiUrl}/card-products/summary`, () => HttpResponse.json({ totalProducts: 2, totalAvailableInventory: 4 })),
  http.get(`${apiUrl}/exchange-rates/currencies`, () => HttpResponse.json({ detail: 'Exchange-rate provider is currently unavailable' }, { status: 503 })),
  http.post(`${apiUrl}/card-products`, () => HttpResponse.json({ id: 3 }, { status: 201 })),
  http.put(`${apiUrl}/card-products/:id`, () => HttpResponse.json({ id: 1 }, { status: 200 })),
  http.delete(`${apiUrl}/card-products/:id`, () => new HttpResponse(null, { status: 204 })),
)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => {
  filteredRequestObserved = false
  server.resetHandlers()
})
afterAll(() => server.close())

describe('App integration', () => {
  it('loads dashboard data and keeps prices in MYR when the currency provider is unavailable', async () => {
    render(<App />)

    expect(await screen.findByText('CardOps Platinum')).toBeVisible()
    expect(screen.getByText('Total products')).toBeVisible()
    expect(await screen.findByText('2')).toBeVisible()

    expect(await screen.findByText('Currency options are unavailable. Prices are shown in MYR.')).toBeVisible()
    expect(screen.getByPlaceholderText('Currency')).toHaveValue('RM MYR')
  })

  it('creates, updates, and deletes products through the dashboard', async () => {
    render(<App />)
    await screen.findByText('CardOps Platinum')

    fireEvent.click(screen.getByRole('button', { name: 'Add product' }))
    fireEvent.change(screen.getByLabelText(/Product name/), { target: { value: 'CardOps Gold' } })
    fireEvent.change(screen.getByLabelText(/Quantity/), { target: { value: '5' } })
    fireEvent.change(screen.getByLabelText(/Annual fee \(MYR\)/), { target: { value: '150' } })
    fireEvent.click(screen.getByRole('button', { name: 'Create product' }))
    expect(await screen.findByText('Product created.')).toBeVisible()
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: 'Increase CardOps Platinum quantity' }))
    expect(await screen.findByText('Quantity increased.')).toBeVisible()

    fireEvent.click(screen.getByRole('button', { name: 'Delete CardOps Debit' }))
    fireEvent.click(screen.getByRole('button', { name: 'Delete' }))
    expect(await screen.findByText('Product deleted.')).toBeVisible()
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())

    fireEvent.change(screen.getByLabelText('Search product name'), { target: { value: 'Plat' } })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))
    await waitFor(() => expect(filteredRequestObserved).toBe(true))
  }, 30_000)

  it('uses a successfully retrieved rate for the selected display currency', async () => {
    server.use(
      http.get(`${apiUrl}/exchange-rates/currencies`, () => HttpResponse.json([
        { code: 'MYR', name: 'Malaysian Ringgit', symbol: 'RM' },
        { code: 'USD', name: 'United States Dollar', symbol: '$' },
      ])),
      http.get(`${apiUrl}/exchange-rates`, () => HttpResponse.json({ baseCurrency: 'MYR', targetCurrency: 'USD', rate: 0.24, rateDate: '2026-10-22' })),
    )
    render(<App />)
    await screen.findByText('CardOps Platinum')

    fireEvent.mouseDown(screen.getByPlaceholderText('Currency'))
    fireEvent.click(await screen.findByRole('option', { name: /\$ USD, United States dollar/i }))

    await waitFor(() => expect(screen.getByPlaceholderText('Currency')).toHaveValue('$ USD'))
  })
})
