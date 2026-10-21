package com.assessment.card_product_management.cardproduct.dto;

import com.assessment.card_product_management.cardproduct.CardProduct;
import com.assessment.card_product_management.cardproduct.CardType;

import java.time.LocalDateTime;

public record CardProductResponse(
        Long id,
        String productName,
        CardType cardType,
        int quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version
) {
    public static CardProductResponse from(CardProduct product) {
        return new CardProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCardType(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getVersion()
        );
    }
}
