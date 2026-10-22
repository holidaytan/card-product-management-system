import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    fileParallelism: false,
    css: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      exclude: ['src/main.tsx', 'src/theme.ts'],
      thresholds: {
        statements: 80,
        functions: 80,
        lines: 80,
      },
    },
  },
})
