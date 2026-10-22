import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ProductDialog } from './ProductDialog'

describe('ProductDialog', () => {
  it('shows client-side validation errors for an empty submission', async () => {
    const user = userEvent.setup()
    render(<ProductDialog open product={null} submitting={false} onClose={vi.fn()} onSubmit={vi.fn()} />)

    await user.click(screen.getByRole('button', { name: 'Create product' }))

    expect(screen.getByText('Product name is required.')).toBeVisible()
    expect(screen.getByText('Enter a non-negative whole number.')).toBeVisible()
    expect(screen.getByText('Enter a non-negative amount with up to two decimal places.')).toBeVisible()
  })

  it('submits a valid product with an optional interest rate omitted', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    render(<ProductDialog open product={null} submitting={false} onClose={vi.fn()} onSubmit={onSubmit} />)

    fireEvent.change(screen.getByLabelText(/Product name/), { target: { value: 'CardOps Gold' } })
    fireEvent.change(screen.getByLabelText(/Quantity/), { target: { value: '10' } })
    fireEvent.change(screen.getByLabelText(/Annual fee \(MYR\)/), { target: { value: '25.50' } })
    fireEvent.click(screen.getByRole('button', { name: 'Create product' }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith({
      productName: 'CardOps Gold', cardType: 'CREDIT', quantity: 10, annualFee: 25.5, annualInterestRate: null,
    }))
  })
})
