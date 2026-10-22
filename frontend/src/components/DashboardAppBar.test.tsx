import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { DashboardAppBar } from './DashboardAppBar'

const currencies = [
  { code: 'MYR', name: 'Malaysian Ringgit', symbol: 'RM' },
  { code: 'USD', name: 'UNITED STATES DOLLAR', symbol: null },
]

describe('DashboardAppBar', () => {
  it('displays currency information and reports a selected currency', async () => {
    const onCurrencyChange = vi.fn()
    render(<DashboardAppBar currencies={currencies} selectedCurrency={currencies[0]} loading={false} onCurrencyChange={onCurrencyChange} />)

    expect(screen.getByRole('heading', { name: 'CardOps' })).toBeVisible()
    expect(screen.getByPlaceholderText('Currency')).toHaveValue('RM MYR')
    fireEvent.mouseDown(screen.getByPlaceholderText('Currency'))
    const usdOption = await screen.findByRole('option', { name: /USD, United States dollar/i })
    fireEvent.click(usdOption)

    expect(onCurrencyChange).toHaveBeenCalledWith(currencies[1])
  })
})
