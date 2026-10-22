import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material'
import type { CardProduct } from '../api/types'

interface DeleteProductDialogProps {
  product: CardProduct | null
  deleting: boolean
  onClose: () => void
  onConfirm: () => Promise<void>
}

export function DeleteProductDialog({ product, deleting, onClose, onConfirm }: DeleteProductDialogProps) {
  const confirm = async () => {
    try {
      await onConfirm()
    } catch {
      // The parent reports the error in its shared snackbar.
    }
  }
  return (
    <Dialog open={product !== null} onClose={deleting ? undefined : onClose} aria-labelledby="delete-product-dialog-title">
      <DialogTitle id="delete-product-dialog-title">Delete product?</DialogTitle>
      <DialogContent><DialogContentText>This will permanently delete {product?.productName}. This action cannot be undone.</DialogContentText></DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={deleting}>Cancel</Button>
        <Button color="error" variant="contained" onClick={() => { void confirm() }} disabled={deleting}>{deleting ? 'Deleting…' : 'Delete'}</Button>
      </DialogActions>
    </Dialog>
  )
}
