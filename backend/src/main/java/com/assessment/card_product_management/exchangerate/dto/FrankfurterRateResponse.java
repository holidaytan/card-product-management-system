package com.assessment.card_product_management.exchangerate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FrankfurterRateResponse(
        LocalDate date,
        String base,
        String quote,
        BigDecimal rate
) {
}
