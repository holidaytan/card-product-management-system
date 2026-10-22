import { act, renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/types'

const api = vi.hoisted(() => ({ createCardProduct: vi.fn(), updateCardProduct: vi.fn(), deleteCardProduct: vi.fn() }))
vi.mock('../api/client', () => api)

import { useCardProductMutations } from './useCardProductMutations'

const request = { productName: 'CardOps Gold', cardType: 'CREDIT' as const, quantity: 1, annualFee: 120, annualInterestRate: 18 }

describe('useCardProductMutations', () => {
  beforeEach(() => vi.clearAllMocks())

  it('calls the API and reports a successful create', async () => {
    api.createCardProduct.mockResolvedValue({ id: 1 })
    const onSuccess = vi.fn()
    const { result } = renderHook(() => useCardProductMutations({ onSuccess, onConflict: vi.fn() }))

    await act(async () => { await result.current.create(request, 'request-key') })

    expect(api.createCardProduct).toHaveBeenCalledWith(request, 'request-key')
    expect(onSuccess).toHaveBeenCalledWith('create')
    expect(result.current.pendingAction).toBeNull()
  })

  it('reports conflicts and clears the pending action after a failed update', async () => {
    api.updateCardProduct.mockRejectedValue(new ApiError('Conflict', 409))
    const onConflict = vi.fn()
    const { result } = renderHook(() => useCardProductMutations({ onSuccess: vi.fn(), onConflict }))

    await expect(act(async () => { await result.current.update(1, { ...request, version: 0 }) })).rejects.toThrow('Conflict')

    await waitFor(() => expect(onConflict).toHaveBeenCalledOnce())
    expect(result.current.pendingAction).toBeNull()
  })
})
