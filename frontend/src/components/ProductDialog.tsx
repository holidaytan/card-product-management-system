import { useEffect, useRef, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import CloseIcon from '@mui/icons-material/Close'
import CreditCardOutlinedIcon from '@mui/icons-material/CreditCardOutlined'
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined'
import { Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, Stack, TextField, Typography } from '@mui/material'
import type { CardProduct, CardType, CreateCardProductRequest } from '../api/types'

interface ProductDialogProps {
  open: boolean
  product: CardProduct | null
  submitting: boolean
  onClose: () => void
  onSubmit: (request: CreateCardProductRequest) => Promise<void>
}

type FormState = { productName: string; cardType: CardType; quantity: string; annualFee: string; annualInterestRate: string }
type FieldErrors = Partial<Record<keyof FormState, string>>

const emptyForm: FormState = { productName: '', cardType: 'CREDIT', quantity: '', annualFee: '', annualInterestRate: '' }

function formFromProduct(product: CardProduct | null): FormState {
  if (!product) return emptyForm
  return {
    productName: product.productName,
    cardType: product.cardType,
    quantity: String(product.quantity),
    annualFee: String(product.annualFee),
    annualInterestRate: product.annualInterestRate === null ? '' : String(product.annualInterestRate),
  }
}

function messageFrom(error: unknown): string {
  if (error instanceof Error && 'errors' in error && Array.isArray(error.errors) && error.errors.length > 0) return error.errors.join(' ')
  return error instanceof Error ? error.message : 'The product could not be saved.'
}

export function ProductDialog({ open, product, submitting, onClose, onSubmit }: ProductDialogProps) {
  const contentRef = useRef<HTMLDivElement>(null)
  const [form, setForm] = useState<FormState>(() => formFromProduct(product))
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [submitError, setSubmitError] = useState<string | null>(null)
  const isEdit = product !== null
  const title = isEdit ? 'Edit product' : 'Add product'
  const subtitle = isEdit ? 'Update the selected card product.' : 'Create a new credit or debit card product.'

  useEffect(() => {
    if (open) contentRef.current?.scrollTo({ top: 0 })
  }, [open, product?.id])

  const updateField = <Key extends keyof FormState>(field: Key, value: FormState[Key]) => {
    setForm((current) => {
      const next = { ...current, [field]: value }
      if (field === 'cardType' && value === 'DEBIT') next.annualInterestRate = ''
      return next
    })
    setFieldErrors((current) => {
      const next = { ...current, [field]: undefined }
      if (field === 'cardType' && value === 'DEBIT') next.annualInterestRate = undefined
      return next
    })
  }

  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitError(null)
    const errors: FieldErrors = {}
    const productName = form.productName.trim()
    const quantity = Number(form.quantity)
    const annualFee = Number(form.annualFee)
    const annualInterestRate = form.cardType === 'DEBIT' || form.annualInterestRate.trim() === '' ? null : Number(form.annualInterestRate)

    if (!productName) errors.productName = 'Product name is required.'
    else if (productName.length > 150) errors.productName = 'Product name must be 150 characters or fewer.'
    if (!/^\d+$/.test(form.quantity.trim())) errors.quantity = 'Enter a non-negative whole number.'
    if (!/^\d+(\.\d{1,2})?$/.test(form.annualFee.trim()) || !Number.isFinite(annualFee) || annualFee < 0) errors.annualFee = 'Enter a non-negative amount with up to two decimal places.'
    if (annualInterestRate !== null && (!Number.isFinite(annualInterestRate) || annualInterestRate < 0 || annualInterestRate > 100)) errors.annualInterestRate = 'Enter a value from 0 to 100.'
    if (!Number.isInteger(quantity) || quantity < 0) errors.quantity = 'Enter a non-negative whole number.'
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    try {
      await onSubmit({ productName, cardType: form.cardType, quantity, annualFee, annualInterestRate })
    } catch (error) {
      setSubmitError(messageFrom(error))
    }
  }

  const actionIcon = submitting ? <CircularProgress size={16} color="inherit" aria-label="Saving" /> : isEdit ? <SaveOutlinedIcon fontSize="small" /> : <AddIcon fontSize="small" />

  return (
    <Dialog open={open} onClose={submitting ? undefined : onClose} fullWidth maxWidth={false} scroll="paper" aria-labelledby="product-dialog-title" aria-describedby="product-dialog-description" slotProps={{ paper: { sx: { width: 'min(calc(100% - 32px), 760px)', maxHeight: 'calc(100dvh - 32px)' } } }}>
      <DialogTitle sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.25, px: { xs: 2, sm: 3 }, py: 1.5, borderBottom: 1, borderColor: 'divider' }}>
        <CreditCardOutlinedIcon aria-hidden="true" color="primary" sx={{ mt: 0.25 }} />
        <Box sx={{ flexGrow: 1, minWidth: 0 }}><Typography id="product-dialog-title" component="div" variant="h6" sx={{ fontWeight: 700, lineHeight: 1.25 }}>{title}</Typography><Typography id="product-dialog-description" variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>{subtitle}</Typography></Box>
        <IconButton aria-label="Close product dialog" onClick={onClose} disabled={submitting} size="small" sx={{ mt: -0.25 }}><CloseIcon fontSize="small" /></IconButton>
      </DialogTitle>
      <DialogContent ref={contentRef} sx={{ px: { xs: 2, sm: 3 }, pt: 2.5, pb: 2 }}>
        <Box component="form" id="product-form" onSubmit={submit} noValidate sx={{ pt: 1.5 }}>
          <Stack spacing={2}>
            {submitError && <Alert severity="error">{submitError}</Alert>}
            <Grid container spacing={2}>
              <Grid size={{ xs: 12 }}><TextField autoFocus fullWidth required label="Product name" placeholder="e.g. CardOps Platinum" value={form.productName} error={Boolean(fieldErrors.productName)} helperText={fieldErrors.productName} onChange={(event) => updateField('productName', event.target.value)} slotProps={{ htmlInput: { maxLength: 150 } }} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField select fullWidth required label="Card type" value={form.cardType} onChange={(event) => updateField('cardType', event.target.value as CardType)}><MenuItem value="CREDIT">Credit</MenuItem><MenuItem value="DEBIT">Debit</MenuItem></TextField></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth required label="Quantity" placeholder="0" type="number" value={form.quantity} error={Boolean(fieldErrors.quantity)} helperText={fieldErrors.quantity} onChange={(event) => updateField('quantity', event.target.value)} slotProps={{ htmlInput: { min: 0, step: 1 } }} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth required label="Annual fee (MYR)" placeholder="0.00" type="number" value={form.annualFee} error={Boolean(fieldErrors.annualFee)} helperText={fieldErrors.annualFee ?? 'Stored in MYR.'} onChange={(event) => updateField('annualFee', event.target.value)} slotProps={{ htmlInput: { min: 0, step: '0.01' } }} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Annual interest rate (%)" placeholder="Optional" type="number" value={form.annualInterestRate} disabled={form.cardType === 'DEBIT'} error={Boolean(fieldErrors.annualInterestRate)} helperText={fieldErrors.annualInterestRate ?? (form.cardType === 'DEBIT' ? 'Not applicable to debit cards.' : 'Optional for credit cards (0–100%).')} onChange={(event) => updateField('annualInterestRate', event.target.value)} slotProps={{ htmlInput: { min: 0, max: 100, step: '0.01' } }} /></Grid>
            </Grid>
          </Stack>
        </Box>
      </DialogContent>
      <DialogActions sx={{ gap: 1, justifyContent: 'flex-end', px: { xs: 2, sm: 3 }, py: 1, borderTop: 1, borderColor: 'divider' }}>
        <Button onClick={onClose} disabled={submitting}>Cancel</Button>
        <Button type="submit" form="product-form" variant="contained" disabled={submitting} startIcon={actionIcon} sx={{ minHeight: 40, borderRadius: 2, boxShadow: 'none', '&:hover': { boxShadow: 'none' } }}>{submitting ? (isEdit ? 'Saving changes' : 'Creating product') : (isEdit ? 'Save changes' : 'Create product')}</Button>
      </DialogActions>
    </Dialog>
  )
}
