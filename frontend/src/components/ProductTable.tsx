import AddIcon from '@mui/icons-material/Add'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import RemoveIcon from '@mui/icons-material/Remove'
import { Box, Chip, IconButton, Paper, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from '@mui/material'
import type { CardProduct } from '../api/types'

interface ProductTableProps {
  products: CardProduct[]
  loading: boolean
  emptyMessage: string
  displayAnnualFee: (fee: number) => string
  mutationPending: boolean
  onEdit: (product: CardProduct) => void
  onChangeQuantity: (product: CardProduct, amount: number) => void
  onDelete: (product: CardProduct) => void
}

export function ProductTable({ products, loading, emptyMessage, displayAnnualFee, mutationPending, onEdit, onChangeQuantity, onDelete }: ProductTableProps) {
  const iconButtonSx = {
    width: 36,
    height: 36,
    '&:hover': { backgroundColor: 'action.hover' },
  }

  return (
    <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
      <Table aria-label="Card products" sx={{ minWidth: 720 }}>
        <TableHead><TableRow><TableCell>Product</TableCell><TableCell>Type</TableCell><TableCell align="right">Quantity</TableCell><TableCell align="right">Annual fee</TableCell><TableCell align="right">Interest rate</TableCell><TableCell align="right">Actions</TableCell></TableRow></TableHead>
        <TableBody>
          {loading && Array.from({ length: 5 }, (_, index) => <TableRow key={index}><TableCell colSpan={6}><Skeleton height={34} /></TableCell></TableRow>)}
          {!loading && products.length === 0 && <TableRow><TableCell colSpan={6} align="center" sx={{ py: 7 }}><Typography color="text.secondary">{emptyMessage}</Typography></TableCell></TableRow>}
          {!loading && products.map((product) => <TableRow key={product.id} hover sx={{ '& > *': { py: 0.75 }, height: 68 }}><TableCell sx={{ fontWeight: 600 }}>{product.productName}</TableCell><TableCell><Chip size="small" label={product.cardType} color={product.cardType === 'CREDIT' ? 'primary' : 'default'} variant="outlined" /></TableCell><TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
            <Box sx={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'flex-end', gap: 0.25 }}>
              <Tooltip title="Decrease quantity"><span><IconButton aria-label={`Decrease ${product.productName} quantity`} size="small" onClick={() => onChangeQuantity(product, -1)} disabled={mutationPending || product.quantity === 0} sx={iconButtonSx}><RemoveIcon fontSize="small" /></IconButton></span></Tooltip>
              <Typography component="span" aria-label={`${product.productName} quantity: ${product.quantity}`} sx={{ minWidth: 28, fontVariantNumeric: 'tabular-nums', fontWeight: 600, textAlign: 'center' }}>{product.quantity}</Typography>
              <Tooltip title="Increase quantity"><span><IconButton aria-label={`Increase ${product.productName} quantity`} size="small" onClick={() => onChangeQuantity(product, 1)} disabled={mutationPending} sx={iconButtonSx}><AddIcon fontSize="small" /></IconButton></span></Tooltip>
            </Box>
          </TableCell><TableCell align="right" sx={{ fontVariantNumeric: 'tabular-nums' }}>{displayAnnualFee(product.annualFee)}</TableCell><TableCell align="right" sx={{ fontVariantNumeric: 'tabular-nums' }}>{product.cardType === 'DEBIT' ? 'N/A' : `${product.annualInterestRate ?? 0}%`}</TableCell><TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
            <Tooltip title="Edit product"><span><IconButton aria-label={`Edit ${product.productName}`} size="small" onClick={() => onEdit(product)} disabled={mutationPending} sx={iconButtonSx}><EditOutlinedIcon fontSize="small" /></IconButton></span></Tooltip>
            {product.quantity === 0 && <Tooltip title="Delete product"><span><IconButton aria-label={`Delete ${product.productName}`} size="small" color="error" onClick={() => onDelete(product)} disabled={mutationPending} sx={iconButtonSx}><DeleteOutlineIcon fontSize="small" /></IconButton></span></Tooltip>}
          </TableCell></TableRow>)}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
