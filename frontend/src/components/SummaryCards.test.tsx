import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { SummaryCards } from './SummaryCards'

describe('SummaryCards', () => {
  it('formats totals and displays the loading placeholder', () => {
    const { rerender } = render(<SummaryCards totalProducts={1234} totalAvailableInventory={98765} loading={false} />)
    expect(screen.getByText('Total products')).toBeVisible()
    expect(screen.getByText('1,234')).toBeVisible()
    expect(screen.getByText('98,765')).toBeVisible()

    rerender(<SummaryCards totalProducts={null} totalAvailableInventory={null} loading />)
    expect(screen.getAllByText('—')).toHaveLength(2)
  })
})
