package com.assessment.card_product_management.cardproduct.dto;

import com.assessment.card_product_management.cardproduct.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateCardProductRequest(
        @NotBlank @Size(max = 150) String productName,
        @NotNull CardType cardType,
        @NotNull @PositiveOrZero Integer quantity,
        @NotNull @PositiveOrZero Long version
) {
}
