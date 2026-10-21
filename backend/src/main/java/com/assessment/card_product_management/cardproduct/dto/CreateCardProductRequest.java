package com.assessment.card_product_management.cardproduct.dto;

import com.assessment.card_product_management.cardproduct.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateCardProductRequest(
        @NotBlank(message = "productName is required")
        @Size(max = 150, message = "productName must not exceed 150 characters")
        String productName,

        @NotNull(message = "cardType is required")
        CardType cardType,

        @NotNull(message = "quantity is required")
        @PositiveOrZero(message = "quantity must be zero or greater")
        Integer quantity
) {
}
