import { useCallback, useState } from 'react'
import { createCardProduct, deleteCardProduct, updateCardProduct } from '../api/client'
import type { CreateCardProductRequest, UpdateCardProductRequest } from '../api/types'

export type ProductMutationAction = 'create' | 'update' | 'delete'

interface UseCardProductMutationsOptions {
  onSuccess: (action: ProductMutationAction) => void
  onConflict: () => void
}

export function useCardProductMutations({ onSuccess, onConflict }: UseCardProductMutationsOptions) {
  const [pendingAction, setPendingAction] = useState<ProductMutationAction | null>(null)
  const run = useCallback(async <T,>(action: ProductMutationAction, operation: () => Promise<T>): Promise<T> => {
    setPendingAction(action)
    try {
      const result = await operation()
      onSuccess(action)
      return result
    } catch (error) {
      if (error instanceof Error && 'status' in error && error.status === 409) onConflict()
      throw error
    } finally {
      setPendingAction(null)
    }
  }, [onConflict, onSuccess])

  return {
    pendingAction,
    create: (request: CreateCardProductRequest, idempotencyKey: string) => run('create', () => createCardProduct(request, idempotencyKey)),
    update: (id: number, request: UpdateCardProductRequest) => run('update', () => updateCardProduct(id, request)),
    remove: (id: number) => run('delete', () => deleteCardProduct(id)),
  }
}
