package com.assessment.card_product_management.exchangerate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateResponse(
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        LocalDate rateDate
) {
}
