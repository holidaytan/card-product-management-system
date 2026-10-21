package com.assessment.card_product_management.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FrankfurterCurrencyResponse(
        @JsonProperty("iso_code") String isoCode,
        String name,
        String symbol
) {
}
