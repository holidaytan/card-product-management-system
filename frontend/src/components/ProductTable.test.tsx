import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ProductTable } from './ProductTable'

const creditProduct = { id: 1, productName: 'CardOps Gold', cardType: 'CREDIT' as const, quantity: 2, annualFee: 120, annualInterestRate: 18, currencyCode: 'MYR', createdAt: '', updatedAt: '', version: 0 }
const debitProduct = { ...creditProduct, id: 2, productName: 'CardOps Debit', cardType: 'DEBIT' as const, quantity: 0, annualInterestRate: null }

describe('ProductTable', () => {
  it('renders products and sends actions for an available credit product', () => {
    const onEdit = vi.fn()
    const onChangeQuantity = vi.fn()
    const onDelete = vi.fn()
    render(<ProductTable products={[creditProduct]} loading={false} emptyMessage="No products" displayAnnualFee={(fee) => `RM ${fee}`} mutationPending={false} onEdit={onEdit} onChangeQuantity={onChangeQuantity} onDelete={onDelete} />)

    expect(screen.getByText('CardOps Gold')).toBeVisible()
    expect(screen.getByText('RM 120')).toBeVisible()
    expect(screen.getByText('18%')).toBeVisible()
    fireEvent.click(screen.getByRole('button', { name: 'Decrease CardOps Gold quantity' }))
    fireEvent.click(screen.getByRole('button', { name: 'Increase CardOps Gold quantity' }))
    fireEvent.click(screen.getByRole('button', { name: 'Edit CardOps Gold' }))
    expect(onChangeQuantity).toHaveBeenNthCalledWith(1, creditProduct, -1)
    expect(onChangeQuantity).toHaveBeenNthCalledWith(2, creditProduct, 1)
    expect(onEdit).toHaveBeenCalledWith(creditProduct)
    expect(screen.queryByRole('button', { name: 'Delete CardOps Gold' })).not.toBeInTheDocument()
  })

  it('handles the zero-quantity debit state and empty/loading states', () => {
    const onDelete = vi.fn()
    const { rerender } = render(<ProductTable products={[debitProduct]} loading={false} emptyMessage="No products" displayAnnualFee={(fee) => `RM ${fee}`} mutationPending={false} onEdit={vi.fn()} onChangeQuantity={vi.fn()} onDelete={onDelete} />)

    expect(screen.getByText('N/A')).toBeVisible()
    expect(screen.getByRole('button', { name: 'Decrease CardOps Debit quantity' })).toBeDisabled()
    fireEvent.click(screen.getByRole('button', { name: 'Delete CardOps Debit' }))
    expect(onDelete).toHaveBeenCalledWith(debitProduct)

    rerender(<ProductTable products={[]} loading={false} emptyMessage="No products" displayAnnualFee={String} mutationPending={false} onEdit={vi.fn()} onChangeQuantity={vi.fn()} onDelete={vi.fn()} />)
    expect(screen.getByText('No products')).toBeVisible()
    rerender(<ProductTable products={[]} loading emptyMessage="No products" displayAnnualFee={String} mutationPending={false} onEdit={vi.fn()} onChangeQuantity={vi.fn()} onDelete={vi.fn()} />)
    expect(screen.getAllByText('', { selector: '.MuiSkeleton-root' })).toHaveLength(5)
  })
})
