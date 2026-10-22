import CreditCardOutlinedIcon from '@mui/icons-material/CreditCardOutlined'
import { AppBar, Autocomplete, Box, createFilterOptions, Stack, TextField, Toolbar, Typography } from '@mui/material'
import type { Currency } from '../api/types'

const currencyNameTerms = new Set([
  'afghani', 'and', 'baht', 'balboa', 'birr', 'bolivar', 'cordoba', 'convertible', 'dalasi', 'dinar', 'dirham', 'dobra', 'dollar',
  'dong', 'drawing', 'escudo', 'euro', 'forint', 'franc', 'guarani', 'hryvnia', 'kip', 'koruna', 'krone', 'krona', 'kwacha', 'kwanza',
  'kyat', 'lari', 'lek', 'leu', 'lev', 'lira', 'manat', 'mark', 'metical', 'naira', 'nakfa', 'of', 'ouguiya', 'pataca', 'peso', 'pound',
  'pula', 'quetzal', 'rand', 'real', 'rial', 'riel', 'ringgit', 'rights', 'riyal', 'rubel', 'ruble', 'rupee', 'rupiah', 'shilling', 'sol',
  'som', 'special', 'taka', 'tenge', 'the', 'tugrik', 'won', 'yuan', 'yen', 'zloty',
])

function currencySymbol(currency: Currency) {
  if (currency.symbol?.trim()) return currency.symbol.trim()
  try {
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: currency.code, currencyDisplay: 'narrowSymbol' })
      .formatToParts(0).find((part) => part.type === 'currency')?.value ?? currency.code
  } catch {
    return currency.code
  }
}

function sentenceCaseCurrencyName(name: string) {
  return name.trim().split(/\s+/).map((word, index) => {
    const lower = word.toLocaleLowerCase()
    if (index === 0) return `${lower.charAt(0).toLocaleUpperCase()}${lower.slice(1)}`
    if (word === word.toLocaleUpperCase() && /[A-Z]/.test(word)) return word
    return currencyNameTerms.has(lower) ? lower : word
  }).join(' ')
}

const filterCurrencies = createFilterOptions<Currency>({
  stringify: (currency) => `${currencySymbol(currency)} ${currency.code} ${currency.name} ${sentenceCaseCurrencyName(currency.name)}`,
})

interface DashboardAppBarProps {
  currencies: Currency[]
  selectedCurrency: Currency
  loading: boolean
  onCurrencyChange: (currency: Currency | null) => void
}

export function DashboardAppBar({ currencies, selectedCurrency, loading, onCurrencyChange }: DashboardAppBarProps) {
  return (
    <AppBar position="sticky" elevation={0}>
      <Toolbar sx={{ gap: { xs: 1, sm: 2 }, minHeight: 64, px: { xs: 2, sm: 3 } }}>
        <Box aria-hidden="true" sx={{ display: 'grid', flex: '0 0 auto', placeItems: 'center', width: 32, height: 32 }}>
          <CreditCardOutlinedIcon sx={{ color: '#7dd3fc' }} />
        </Box>
        <Stack spacing={0} sx={{ flexGrow: 1, minWidth: 0, lineHeight: 1.15 }}>
          <Typography component="h1" variant="h6" sx={{ fontWeight: 750, lineHeight: 1.15, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            CardOps
          </Typography>
          <Typography variant="caption" sx={{ color: '#bfdbfe', lineHeight: 1.15, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            Product Administration
          </Typography>
        </Stack>
        <Autocomplete
          aria-label="Display currency"
          disableClearable
          filterOptions={filterCurrencies}
          loading={loading}
          options={currencies}
          value={selectedCurrency}
          isOptionEqualToValue={(option, value) => option.code === value.code}
          getOptionLabel={(option) => `${currencySymbol(option)} ${option.code.toUpperCase()}`}
          renderOption={(props, option) => {
            const { key, ...optionProps } = props
            const symbol = currencySymbol(option)
            const name = sentenceCaseCurrencyName(option.name)
            return <li key={key} {...optionProps} aria-label={`${symbol} ${option.code.toUpperCase()}, ${name}`}><Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 1.25, py: 0.25 }}>
              <Typography component="span" aria-hidden="true" sx={{ flex: '0 0 28px', color: 'text.secondary', fontVariantNumeric: 'tabular-nums', textAlign: 'center' }}>{symbol}</Typography>
              <Box><Typography variant="body2" sx={{ fontWeight: 700, lineHeight: 1.2 }}>{option.code.toUpperCase()}</Typography><Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.25 }}>{name}</Typography></Box>
            </Box></li>
          }}
          onChange={(_, currency) => onCurrencyChange(currency)}
          slotProps={{ popper: { placement: 'bottom-end', style: { width: 'min(310px, calc(100vw - 32px))' } }, paper: { sx: { width: '100%' } } }}
          sx={{ flex: '0 1 auto', width: { xs: 'min(52vw, 180px)', sm: 190 }, '& .MuiOutlinedInput-root': { backgroundColor: '#fff', minHeight: 40 } }}
          renderInput={(params) => <TextField {...params} size="small" placeholder="Currency" aria-label="Display currency" />}
        />
      </Toolbar>
    </AppBar>
  )
}
