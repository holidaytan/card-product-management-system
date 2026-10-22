import { createTheme } from '@mui/material/styles'

export const theme = createTheme({
  palette: {
    primary: { main: '#0b1f3a', dark: '#06152a', light: '#1f4f8c' },
    secondary: { main: '#1769aa' },
    background: { default: '#f4f7fb', paper: '#ffffff' },
  },
  shape: { borderRadius: 10 },
  typography: { fontFamily: 'Inter, Roboto, Arial, sans-serif', h4: { fontWeight: 700 } },
  components: {
    MuiAppBar: { styleOverrides: { root: { backgroundImage: 'none' } } },
    MuiTableCell: { styleOverrides: { head: { fontWeight: 700, color: '#334155', backgroundColor: '#f8fafc' } } },
  },
})
