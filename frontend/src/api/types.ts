export type CardType = 'CREDIT' | 'DEBIT'

export interface CardProduct {
  id: number
  productName: string
  cardType: CardType
  quantity: number
  annualFee: number
  annualInterestRate: number | null
  currencyCode: string
  createdAt: string
  updatedAt: string
  version: number
}

export interface CreateCardProductRequest {
  productName: string
  cardType: CardType
  quantity: number
  annualFee: number
  annualInterestRate: number | null
}

export interface UpdateCardProductRequest extends CreateCardProductRequest {
  version: number
}

export interface CardProductPage {
  content: CardProduct[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface CardProductSummary {
  totalProducts: number
  totalAvailableInventory: number
}

export interface Currency {
  code: string
  name: string
  symbol: string | null
}

export interface ExchangeRate {
  baseCurrency: string
  targetCurrency: string
  rate: number
  rateDate: string
}

export class ApiError extends Error {
  readonly status: number
  readonly errors: string[]

  constructor(message: string, status: number, errors: string[] = []) {
    super(message)
    this.status = status
    this.errors = errors
  }
}
