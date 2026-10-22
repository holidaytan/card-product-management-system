import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { DeleteProductDialog } from './DeleteProductDialog'

const product = { id: 1, productName: 'CardOps Gold', cardType: 'CREDIT' as const, quantity: 0, annualFee: 120, annualInterestRate: 18, currencyCode: 'MYR', createdAt: '', updatedAt: '', version: 0 }

describe('DeleteProductDialog', () => {
  it('confirms deletion and allows cancellation when not pending', async () => {
    const onClose = vi.fn()
    const onConfirm = vi.fn().mockResolvedValue(undefined)
    render(<DeleteProductDialog product={product} deleting={false} onClose={onClose} onConfirm={onConfirm} />)

    expect(screen.getByText(/permanently delete CardOps Gold/)).toBeVisible()
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    fireEvent.click(screen.getByRole('button', { name: 'Delete' }))
    expect(onClose).toHaveBeenCalledOnce()
    await waitFor(() => expect(onConfirm).toHaveBeenCalledOnce())
  })

  it('disables actions while deletion is pending', () => {
    render(<DeleteProductDialog product={product} deleting onClose={vi.fn()} onConfirm={vi.fn()} />)
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeDisabled()
    expect(screen.getByRole('button', { name: /Deleting/ })).toBeDisabled()
  })
})
