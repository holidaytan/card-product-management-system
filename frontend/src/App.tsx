import { useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, Container, MenuItem, Pagination, Paper, Snackbar, Stack, TextField, Typography, useMediaQuery } from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import RefreshIcon from '@mui/icons-material/Refresh'
import RestartAltIcon from '@mui/icons-material/RestartAlt'
import SearchIcon from '@mui/icons-material/Search'
import { DashboardAppBar } from './components/DashboardAppBar'
import { DeleteProductDialog } from './components/DeleteProductDialog'
import { ProductDialog } from './components/ProductDialog'
import { ProductTable } from './components/ProductTable'
import { SummaryCards } from './components/SummaryCards'
import { getExchangeRate } from './api/client'
import type { ProductDirectoryFilters } from './api/client'
import { ApiError } from './api/types'
import type { CardProduct, CardType, CreateCardProductRequest, Currency, ExchangeRate } from './api/types'
import { useCardProducts } from './hooks/useCardProducts'
import { useCardProductMutations } from './hooks/useCardProductMutations'
import { useCurrencyCatalogue } from './hooks/useCurrencyCatalogue'
import { useProductSummary } from './hooks/useProductSummary'
import './App.css'

const myr: Currency = { code: 'MYR', name: 'Malaysian Ringgit', symbol: 'RM' }
const emptyDirectoryFilters: ProductDirectoryFilters = { name: '', cardType: null }

function newIdempotencyKey() {
  return crypto.randomUUID()
}

function errorMessage(error: unknown) {
  if (error instanceof ApiError && error.errors.length > 0) return error.errors.join(' ')
  return error instanceof Error ? error.message : 'The request could not be completed.'
}

function App() {
  const [page, setPage] = useState(0)
  const [searchInput, setSearchInput] = useState('')
  const [cardTypeInput, setCardTypeInput] = useState<'ALL' | CardType>('ALL')
  const [directoryFilters, setDirectoryFilters] = useState<ProductDirectoryFilters>(emptyDirectoryFilters)
  const { data: productPage, loading: productsLoading, error: productsError, reload } = useCardProducts(page, directoryFilters)
  const { data: summary, loading: summaryLoading, error: summaryError, reload: reloadSummary } = useProductSummary()
  const { currencies, loading: catalogueLoading, error: catalogueError, retry: retryCatalogue } = useCurrencyCatalogue()
  const [selectedCurrency, setSelectedCurrency] = useState<Currency>(myr)
  const [exchangeRate, setExchangeRate] = useState<ExchangeRate | null>(null)
  const [conversionError, setConversionError] = useState<string | null>(null)
  const [productDialog, setProductDialog] = useState<'create' | 'edit' | null>(null)
  const [editingProduct, setEditingProduct] = useState<CardProduct | null>(null)
  const [deleteCandidate, setDeleteCandidate] = useState<CardProduct | null>(null)
  const [createIdempotencyKey, setCreateIdempotencyKey] = useState('')
  const [feedback, setFeedback] = useState<{ severity: 'success' | 'error'; message: string; duration: number } | null>(null)
  const isMobile = useMediaQuery('(max-width:599.95px)')

  const refreshAfterMutation = (action: 'create' | 'update' | 'delete') => {
    if (action === 'delete' && productPage?.content.length === 1 && page > 0) {
      setPage((currentPage) => currentPage - 1)
    } else {
      reload()
    }
    reloadSummary()
  }
  const refreshAfterConflict = () => {
    reload()
    reloadSummary()
  }
  const { pendingAction, create, update, remove } = useCardProductMutations({ onSuccess: refreshAfterMutation, onConflict: refreshAfterConflict })

  useEffect(() => {
    const controller = new AbortController()
    if (selectedCurrency.code === 'MYR') {
      return () => controller.abort()
    }
    getExchangeRate(selectedCurrency.code, controller.signal).then((rate) => {
      if (!controller.signal.aborted) {
        setExchangeRate(rate)
        setConversionError(null)
      }
    }).catch((requestError: unknown) => {
      if (!controller.signal.aborted && !(requestError instanceof DOMException && requestError.name === 'AbortError')) {
        setExchangeRate(null)
        setSelectedCurrency(myr)
        setConversionError(requestError instanceof ApiError ? `${requestError.message} Prices are shown in MYR.` : 'Currency conversion is unavailable. Prices are shown in MYR.')
      }
    })
    return () => controller.abort()
  }, [selectedCurrency.code])

  const effectiveRate = selectedCurrency.code === 'MYR' ? 1 : exchangeRate?.targetCurrency === selectedCurrency.code ? exchangeRate.rate : null
  const annualFeeFormatter = useMemo(() => new Intl.NumberFormat('en-MY', {
    style: 'currency', currency: effectiveRate === null ? 'MYR' : selectedCurrency.code, minimumFractionDigits: 2,
  }), [effectiveRate, selectedCurrency.code])
  const displayAnnualFee = (annualFee: number) => annualFeeFormatter.format(annualFee * (effectiveRate ?? 1))

  const openCreateDialog = () => {
    setEditingProduct(null)
    setCreateIdempotencyKey(newIdempotencyKey())
    setProductDialog('create')
  }
  const openEditDialog = (product: CardProduct) => {
    setEditingProduct(product)
    setProductDialog('edit')
  }
  const saveProduct = async (request: CreateCardProductRequest) => {
    try {
      if (productDialog === 'edit' && editingProduct) {
        await update(editingProduct.id, { ...request, version: editingProduct.version })
        setFeedback({ severity: 'success', message: 'Product updated.', duration: 3500 })
      } else {
        await create(request, createIdempotencyKey)
        setFeedback({ severity: 'success', message: 'Product created.', duration: 3500 })
      }
      setProductDialog(null)
    } catch (error) {
      setFeedback({
        severity: 'error',
        message: error instanceof ApiError && error.status === 409 ? 'This product changed elsewhere. The latest data has been refreshed.' : errorMessage(error), duration: 5500,
      })
      throw error
    }
  }
  const changeQuantity = async (product: CardProduct, amount: number) => {
    if (product.quantity + amount < 0) return
    try {
      await update(product.id, {
        productName: product.productName, cardType: product.cardType, quantity: product.quantity + amount,
        annualFee: product.annualFee, annualInterestRate: product.annualInterestRate, version: product.version,
      })
      setFeedback({ severity: 'success', message: `Quantity ${amount > 0 ? 'increased' : 'decreased'}.`, duration: 2500 })
    } catch (error) {
      setFeedback({ severity: 'error', message: error instanceof ApiError && error.status === 409 ? 'This product changed elsewhere. The latest data has been refreshed.' : errorMessage(error), duration: 5500 })
    }
  }
  const deleteProduct = async () => {
    if (!deleteCandidate || deleteCandidate.quantity !== 0) return
    try {
      await remove(deleteCandidate.id)
      setDeleteCandidate(null)
      setFeedback({ severity: 'success', message: 'Product deleted.', duration: 3500 })
    } catch (error) {
      setFeedback({ severity: 'error', message: error instanceof ApiError && error.status === 409 ? 'This product changed elsewhere. The latest data has been refreshed.' : errorMessage(error), duration: 5500 })
      throw error
    }
  }
  const applyDirectoryFilters = () => {
    const nextFilters: ProductDirectoryFilters = { name: searchInput.trim(), cardType: cardTypeInput === 'ALL' ? null : cardTypeInput }
    const criteriaChanged = nextFilters.name !== directoryFilters.name || nextFilters.cardType !== directoryFilters.cardType
    if (page !== 0) setPage(0)
    if (criteriaChanged) setDirectoryFilters(nextFilters)
    else if (page === 0) reload()
  }
  const resetDirectoryFilters = () => {
    const criteriaChanged = directoryFilters.name !== '' || directoryFilters.cardType !== null
    setSearchInput('')
    setCardTypeInput('ALL')
    if (page !== 0) setPage(0)
    if (criteriaChanged) setDirectoryFilters(emptyDirectoryFilters)
    else if (page === 0) reload()
  }
  const hasDirectoryFilters = directoryFilters.name !== '' || directoryFilters.cardType !== null

  return (
    <Box className="app-shell">
      <DashboardAppBar currencies={currencies} selectedCurrency={selectedCurrency} loading={catalogueLoading} onCurrencyChange={(currency) => { if (currency) { setConversionError(null); setExchangeRate(null); setSelectedCurrency(currency) } }} />
      <Container component="main" maxWidth="lg" sx={{ py: { xs: 2.5, sm: 3 } }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2, justifyContent: 'space-between', alignItems: { sm: 'center' } }}>
          <Box><Typography component="h2" variant="h4">Card products</Typography><Typography color="text.secondary">Manage available credit and debit card products.</Typography></Box>
          <Button variant="contained" size="large" startIcon={<AddIcon />} onClick={openCreateDialog} disabled={pendingAction !== null}>Add product</Button>
        </Stack>
        {catalogueError && <Alert severity="warning" action={<Button color="inherit" size="small" startIcon={<RefreshIcon />} onClick={retryCatalogue} disabled={catalogueLoading}>Retry</Button>} sx={{ mb: 2 }}>Currency options are unavailable. Prices are shown in MYR.</Alert>}
        {!catalogueError && conversionError && <Alert severity="warning" sx={{ mb: 2 }}>{conversionError}</Alert>}
        {productsError && <Alert severity="error" action={<Button color="inherit" size="small" startIcon={<RefreshIcon />} onClick={reload}>Retry</Button>} sx={{ mb: 2 }}>{productsError}</Alert>}
        {summaryError && <Alert severity="warning" action={<Button color="inherit" size="small" startIcon={<RefreshIcon />} onClick={reloadSummary}>Retry</Button>} sx={{ mb: 2 }}>{summaryError}</Alert>}
        <SummaryCards totalProducts={summary?.totalProducts ?? null} totalAvailableInventory={summary?.totalAvailableInventory ?? null} loading={summaryLoading} />
        <Paper component="section" variant="outlined" sx={{ mb: 2, p: { xs: 1.5, sm: 2 } }} aria-labelledby="product-directory-title">
          <Typography id="product-directory-title" variant="h6" sx={{ mb: 1.5, fontWeight: 700 }}>Product directory</Typography>
          <Stack component="form" direction={{ xs: 'column', md: 'row' }} spacing={1.25} onSubmit={(event) => { event.preventDefault(); applyDirectoryFilters() }}>
            <TextField fullWidth size="small" label="Search products" placeholder="Search product name..." value={searchInput} onChange={(event) => setSearchInput(event.target.value)} slotProps={{ htmlInput: { 'aria-label': 'Search product name' } }} />
            <TextField select size="small" label="Card type" value={cardTypeInput} onChange={(event) => setCardTypeInput(event.target.value as 'ALL' | CardType)} sx={{ minWidth: { xs: '100%', md: 150 } }}>
              <MenuItem value="ALL">All types</MenuItem><MenuItem value="CREDIT">Credit</MenuItem><MenuItem value="DEBIT">Debit</MenuItem>
            </TextField>
            <Stack direction="row" spacing={1} sx={{ flex: '0 0 auto' }}><Button type="submit" variant="contained" startIcon={<SearchIcon />} disabled={productsLoading}>Search</Button><Button type="button" variant="outlined" startIcon={<RestartAltIcon />} onClick={resetDirectoryFilters} disabled={productsLoading}>Reset</Button></Stack>
          </Stack>
        </Paper>
        <ProductTable products={productPage?.content ?? []} loading={productsLoading} emptyMessage={hasDirectoryFilters ? 'No products match the current search and filters.' : 'No card products found.'} displayAnnualFee={displayAnnualFee} mutationPending={pendingAction !== null} onEdit={openEditDialog} onChangeQuantity={(product, amount) => { void changeQuantity(product, amount) }} onDelete={setDeleteCandidate} />
        {productPage && productPage.totalPages > 1 && <Stack sx={{ mt: 3, alignItems: 'center' }}><Pagination page={page + 1} count={productPage.totalPages} color="primary" onChange={(_, selectedPage) => setPage(selectedPage - 1)} /></Stack>}
      </Container>
      <ProductDialog key={productDialog === 'edit' ? `edit-${editingProduct?.id}-${editingProduct?.version}` : `create-${createIdempotencyKey}`} open={productDialog !== null} product={productDialog === 'edit' ? editingProduct : null} submitting={pendingAction === 'create' || pendingAction === 'update'} onClose={() => { if (pendingAction === null) setProductDialog(null) }} onSubmit={saveProduct} />
      <DeleteProductDialog product={deleteCandidate} deleting={pendingAction === 'delete'} onClose={() => { if (pendingAction === null) setDeleteCandidate(null) }} onConfirm={deleteProduct} />
      <Snackbar anchorOrigin={{ vertical: 'bottom', horizontal: isMobile ? 'center' : 'right' }} open={feedback !== null} autoHideDuration={feedback?.duration ?? 5500} onClose={(_, reason) => { if (reason !== 'clickaway') setFeedback(null) }}><Alert severity={feedback?.severity ?? 'success'} variant="filled" onClose={() => setFeedback(null)}>{feedback?.message}</Alert></Snackbar>
    </Box>
  )
}

export default App
