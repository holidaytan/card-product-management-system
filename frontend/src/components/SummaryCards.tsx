import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import ViewListOutlinedIcon from '@mui/icons-material/ViewListOutlined'
import { Card, CardContent, Grid, Stack, Typography } from '@mui/material'

interface SummaryCardsProps {
  totalProducts: number | null
  totalAvailableInventory: number | null
  loading: boolean
}

export function SummaryCards({ totalProducts, totalAvailableInventory, loading }: SummaryCardsProps) {
  const formatQuantity = (value: number) => new Intl.NumberFormat().format(value)
  const cards = [
    { label: 'Total products', value: totalProducts, icon: <ViewListOutlinedIcon color="primary" /> },
    { label: 'Total available inventory', value: totalAvailableInventory, icon: <Inventory2OutlinedIcon color="primary" /> },
  ]

  return (
    <Grid container spacing={2} sx={{ mb: 3 }}>
      {cards.map((card) => (
        <Grid key={card.label} size={{ xs: 12, sm: 6 }}>
          <Card variant="outlined"><CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}><Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center' }}><div><Typography color="text.secondary" variant="body2">{card.label}</Typography><Typography variant="h5" sx={{ mt: 0.25, fontSize: '1.65rem', fontWeight: 750, lineHeight: 1.2, fontVariantNumeric: 'tabular-nums' }}>{loading || card.value === null ? '—' : formatQuantity(card.value)}</Typography><Typography color="text.secondary" variant="caption">Across all products</Typography></div>{card.icon}</Stack></CardContent></Card>
        </Grid>
      ))}
    </Grid>
  )
}
